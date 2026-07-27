package com.example.marluse.financeiro;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.financeiro.dto.AbatimentoRequest;
import com.example.marluse.financeiro.dto.AbatimentoResultado;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import com.example.marluse.financeiro.repository.AbatimentoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.financeiro.service.AbatimentoService;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.model.Pedido;
import com.example.marluse.vendas.repository.PedidoRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AbatimentoServiceTest {

    @Autowired private AbatimentoService abatimentoService;
    @Autowired private AbatimentoRepository abatimentoRepository;
    @Autowired private LancamentoFinanceiroRepository lancamentoRepository;
    @Autowired private PedidoRepository pedidoRepository;
    @Autowired private ClienteRepository clienteRepository;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        abatimentoRepository.deleteAll();   // 1º — depende de lançamento
        lancamentoRepository.deleteAll();   // 2º — depende de pedido
        pedidoRepository.deleteAll();       // 3º — depende de cliente
        clienteRepository.deleteAll();

        cliente = clienteRepository.save(Cliente.builder()
                .nome("Devedor").ativo(true).consumidorFinal(false).build());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Pedido pedido(long numero, LocalDate dataMovimento, String valorTotal) {
        return pedidoRepository.save(Pedido.builder()
                .numero(numero)
                .cliente(cliente)
                .status(StatusPedido.CONFIRMADO)
                .formaPagamento(FormaPagamento.FIADO)
                .valorTotal(new BigDecimal(valorTotal))
                .dataMovimento(dataMovimento)
                .build());
    }

    /** Parcela em aberto de um pedido. numParcelas define a ordem dentro do pedido. */
    private LancamentoFinanceiro parcela(Pedido pedido, String valor, LocalDate vencimento, int numParcelas) {
        return lancamentoRepository.save(LancamentoFinanceiro.builder()
                .tipo(TipoLancamento.RECEITA)
                .categoria("Venda")
                .descricao("Pedido #" + pedido.getNumero() + " (" + numParcelas + ")")
                .valor(new BigDecimal(valor))
                .valorPago(BigDecimal.ZERO)
                .status(StatusLancamento.PENDENTE)
                .dataVencimento(vencimento)
                .cliente(cliente)
                .pedido(pedido)
                .numParcelas(numParcelas)
                .totalParcelas(numParcelas)
                .build());
    }

    private LancamentoFinanceiro recarregar(LancamentoFinanceiro l) {
        return lancamentoRepository.findById(l.getId()).orElseThrow();
    }

    private BigDecimal divida() {
        return lancamentoRepository.saldoDevedorPorCliente(cliente.getId());
    }

    private void assertValor(String esperado, BigDecimal atual) {
        assertEquals(0, new BigDecimal(esperado).compareTo(atual),
                () -> "esperado " + esperado + " mas foi " + atual);
    }

    // ── Alocação FIFO ─────────────────────────────────────────────────────────

    @Test
    void debitoAbateDoPedidoMaisAntigoAteCompletar() {
        Pedido antigo = pedido(1L, LocalDate.of(2026, 1, 10), "300.00");
        Pedido novo   = pedido(2L, LocalDate.of(2026, 2, 10), "700.00");
        LancamentoFinanceiro lancAntigo = parcela(antigo, "300.00", LocalDate.of(2026, 1, 10), 1);
        LancamentoFinanceiro lancNovo   = parcela(novo,   "700.00", LocalDate.of(2026, 2, 10), 1);

        // Dívida total = 1000. Debita 100 → tudo sai do pedido mais antigo.
        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("100.00"), null));

        assertValor("1000.00", r.saldoAnterior());
        assertValor("900.00", r.saldoAtual());
        assertValor("100.00", recarregar(lancAntigo).getValorPago());
        assertEquals(StatusLancamento.PENDENTE, recarregar(lancAntigo).getStatus());
        assertValor("0.00", recarregar(lancNovo).getValorPago());  // pedido novo intocado
    }

    @Test
    void debitoTransbordaParaOProximoPedidoAoQuitarOAtual() {
        Pedido antigo = pedido(1L, LocalDate.of(2026, 1, 10), "300.00");
        Pedido novo   = pedido(2L, LocalDate.of(2026, 2, 10), "700.00");
        LancamentoFinanceiro lancAntigo = parcela(antigo, "300.00", LocalDate.of(2026, 1, 10), 1);
        LancamentoFinanceiro lancNovo   = parcela(novo,   "700.00", LocalDate.of(2026, 2, 10), 1);

        // Debita 500 → quita os 300 do antigo e derrama 200 no novo.
        abatimentoService.debitar(cliente.getId(), new AbatimentoRequest(new BigDecimal("500.00"), null));

        LancamentoFinanceiro a = recarregar(lancAntigo);
        assertEquals(StatusLancamento.PAGO, a.getStatus());
        assertValor("300.00", a.getValorPago());
        assertNotNull(a.getDataPagamento());

        LancamentoFinanceiro n = recarregar(lancNovo);
        assertEquals(StatusLancamento.PENDENTE, n.getStatus());
        assertValor("200.00", n.getValorPago());
    }

    /**
     * Regressão: com dois pedidos de MESMA data, o desempate precisa agrupar por origem.
     * Antes o critério era o número da parcela, o que pagava a 1ª parcela de todos os pedidos
     * antes de voltar para a 2ª — em vez de quitar um pedido por vez.
     */
    @Test
    void quitaUmPedidoInteiroAntesDePassarParaOutroDaMesmaData() {
        LocalDate mesmaData = LocalDate.of(2026, 1, 10);
        Pedido pedidoA = pedido(1L, mesmaData, "200.00");
        Pedido pedidoB = pedido(2L, mesmaData, "200.00");

        // Cada pedido em 2 parcelas de 100
        LancamentoFinanceiro a1 = parcela(pedidoA, "100.00", LocalDate.of(2026, 2, 10), 1);
        LancamentoFinanceiro a2 = parcela(pedidoA, "100.00", LocalDate.of(2026, 3, 10), 2);
        LancamentoFinanceiro b1 = parcela(pedidoB, "100.00", LocalDate.of(2026, 2, 10), 1);
        LancamentoFinanceiro b2 = parcela(pedidoB, "100.00", LocalDate.of(2026, 3, 10), 2);

        // Debita 200 = valor de um pedido inteiro
        abatimentoService.debitar(cliente.getId(), new AbatimentoRequest(new BigDecimal("200.00"), null));

        // Um dos pedidos foi quitado por completo; o outro ficou intocado.
        boolean aQuitado = recarregar(a1).getStatus() == StatusLancamento.PAGO
                        && recarregar(a2).getStatus() == StatusLancamento.PAGO;
        boolean bQuitado = recarregar(b1).getStatus() == StatusLancamento.PAGO
                        && recarregar(b2).getStatus() == StatusLancamento.PAGO;

        assertTrue(aQuitado ^ bQuitado,
                "exatamente um pedido deve ter sido quitado por inteiro, sem espalhar entre os dois");

        BigDecimal pagoEmA = recarregar(a1).getValorPago().add(recarregar(a2).getValorPago());
        BigDecimal pagoEmB = recarregar(b1).getValorPago().add(recarregar(b2).getValorPago());
        assertTrue(pagoEmA.signum() == 0 || pagoEmB.signum() == 0,
                "um dos pedidos deveria estar sem nenhum pagamento");
    }

    @Test
    void somaDasParcelasDoAbatimentoBateComOValorDebitado() {
        Pedido antigo = pedido(1L, LocalDate.of(2026, 1, 10), "300.00");
        Pedido novo   = pedido(2L, LocalDate.of(2026, 2, 10), "700.00");
        parcela(antigo, "300.00", LocalDate.of(2026, 1, 10), 1);
        parcela(novo,   "700.00", LocalDate.of(2026, 2, 10), 1);

        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("500.00"), null));

        var abatimento = abatimentoRepository.findById(r.abatimentoId()).orElseThrow();
        BigDecimal somaDasFatias = abatimento.getParcelas().stream()
                .map(p -> p.getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(2, abatimento.getParcelas().size(), "deveria ter atravessado dois pedidos");
        assertValor("500.00", somaDasFatias);
        assertValor("500.00", abatimento.getValor());
    }

    @Test
    void debitoMaiorQueDividaEhRejeitado() {
        Pedido p = pedido(1L, LocalDate.of(2026, 1, 10), "300.00");
        parcela(p, "300.00", LocalDate.of(2026, 1, 10), 1);

        assertThrows(IllegalArgumentException.class, () ->
                abatimentoService.debitar(cliente.getId(),
                        new AbatimentoRequest(new BigDecimal("400.00"), null)));

        assertValor("300.00", divida());  // nada foi alocado
    }

    /** Receita lançada direto no financeiro para o cliente também é dívida (decisão do produto). */
    @Test
    void receitaAvulsaComClienteEntraNaDivida() {
        lancamentoRepository.save(LancamentoFinanceiro.builder()
                .tipo(TipoLancamento.RECEITA)
                .categoria("Serviço")
                .descricao("Conserto avulso")
                .valor(new BigDecimal("50.00"))
                .valorPago(BigDecimal.ZERO)
                .status(StatusLancamento.PENDENTE)
                .dataVencimento(LocalDate.of(2026, 1, 5))
                .cliente(cliente)
                .build());

        assertValor("50.00", divida());

        abatimentoService.debitar(cliente.getId(), new AbatimentoRequest(new BigDecimal("20.00"), null));

        assertValor("30.00", divida());
    }

    // ── Estorno ───────────────────────────────────────────────────────────────

    @Test
    void estornoDevolveOValorExatoAoSaldoDevedor() {
        Pedido antigo = pedido(1L, LocalDate.of(2026, 1, 10), "300.00");
        Pedido novo   = pedido(2L, LocalDate.of(2026, 2, 10), "700.00");
        parcela(antigo, "300.00", LocalDate.of(2026, 1, 10), 1);
        parcela(novo,   "700.00", LocalDate.of(2026, 2, 10), 1);

        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("500.00"), null));
        assertValor("500.00", divida());

        abatimentoService.estornar(r.abatimentoId());

        assertValor("1000.00", divida());  // voltou ao valor original
        assertTrue(abatimentoRepository.findById(r.abatimentoId()).orElseThrow().isEstornado());
    }

    @Test
    void estornoReabreParcelaVencidaComoVencido() {
        LocalDate vencidoOntem = LocalDate.now().minusDays(1);
        Pedido p = pedido(1L, LocalDate.now().minusMonths(2), "300.00");
        LancamentoFinanceiro lanc = parcela(p, "300.00", vencidoOntem, 1);

        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("300.00"), null));
        assertEquals(StatusLancamento.PAGO, recarregar(lanc).getStatus());

        abatimentoService.estornar(r.abatimentoId());

        LancamentoFinanceiro reaberto = recarregar(lanc);
        assertEquals(StatusLancamento.VENCIDO, reaberto.getStatus());  // vencimento já passou
        assertValor("0.00", reaberto.getValorPago());
        assertNull(reaberto.getDataPagamento());
    }

    @Test
    void estornoReabreComoPendenteQuandoAindaNaoVenceu() {
        Pedido p = pedido(1L, LocalDate.now(), "300.00");
        LancamentoFinanceiro lanc = parcela(p, "300.00", LocalDate.now().plusMonths(1), 1);

        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("300.00"), null));
        abatimentoService.estornar(r.abatimentoId());

        assertEquals(StatusLancamento.PENDENTE, recarregar(lanc).getStatus());
    }

    @Test
    void estornoDuplicadoEhRejeitado() {
        Pedido p = pedido(1L, LocalDate.now(), "300.00");
        parcela(p, "300.00", LocalDate.now().plusMonths(1), 1);

        AbatimentoResultado r = abatimentoService.debitar(cliente.getId(),
                new AbatimentoRequest(new BigDecimal("100.00"), null));
        abatimentoService.estornar(r.abatimentoId());

        assertThrows(IllegalArgumentException.class, () -> abatimentoService.estornar(r.abatimentoId()));
        assertValor("300.00", divida());  // não devolveu duas vezes
    }
}
