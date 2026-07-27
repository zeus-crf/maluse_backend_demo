package com.example.marluse.financeiro.service;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.financeiro.dto.AbatimentoRequest;
import com.example.marluse.financeiro.dto.AbatimentoResultado;
import com.example.marluse.financeiro.dto.AbatimentoResumo;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.model.Abatimento;
import com.example.marluse.financeiro.model.AbatimentoParcela;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import com.example.marluse.financeiro.repository.AbatimentoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbatimentoService {

    private final ClienteRepository clienteRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AbatimentoRepository abatimentoRepository;

    @Transactional
    public AbatimentoResultado debitar (String clienteId, AbatimentoRequest request) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        BigDecimal saldoAnterior = lancamentoRepository.saldoDevedorPorCliente(clienteId);
        BigDecimal valor = request.valor();

        if (valor.compareTo(saldoAnterior) > 0) {
            throw new IllegalArgumentException("Valor do débito (" + valor + ") maior que a dívida do cliente (" + saldoAnterior + ")");
        }

        Abatimento abatimento = Abatimento.builder()
                .cliente(cliente)
                .valor(valor)
                .data(LocalDate.now())
                .observacao(request.observacao())
                .estornado(false)
                .build();

        // Alocação FIFO: pedido/locação mais antigo primeiro, completando cada parcela antes de passar.
        List<LancamentoFinanceiro> emAberto = lancamentoRepository.findEmAbertoPorClienteFifo(clienteId);
        BigDecimal restante = valor;

        for (LancamentoFinanceiro lanc : emAberto) {
            if (restante.signum() <= 0) break;

            BigDecimal saldoParcela = lanc.getValor().subtract(lanc.getValorPago());
            if (saldoParcela.signum() <= 0) continue;

            BigDecimal aplicar = restante.min(saldoParcela);
            lanc.setValorPago(lanc.getValorPago().add(aplicar));

            if (lanc.getValorPago().compareTo(lanc.getValor()) >= 0) {
                lanc.setStatus(StatusLancamento.PAGO);
                lanc.setDataPagamento(abatimento.getData());
            }
            lancamentoRepository.save(lanc);

            abatimento.getParcelas().add(AbatimentoParcela.builder()
                    .abatimento(abatimento)
                    .lancamento(lanc)
                    .valor(aplicar)
                    .build());

            restante = restante.subtract(aplicar);
        }

        abatimentoRepository.save(abatimento);

        BigDecimal saldoAtual = lancamentoRepository.saldoDevedorPorCliente(clienteId);
        return AbatimentoResultado.from(abatimento, saldoAnterior, saldoAtual);
    }

    public List<AbatimentoResumo> listarAbatimentos(String clienteId) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        return abatimentoRepository.findByClienteIdOrderByDataDesc(cliente.getId())
                .stream()
                .map(AbatimentoResumo::from)
                .toList();
    }


    @Transactional
    public void estornar(String abatimentoId){
        Abatimento abatimento = abatimentoRepository.findById(abatimentoId)
                .orElseThrow(() -> new EntityNotFoundException("Abatimento não encontrado"));

        if (abatimento.isEstornado()){
            throw new IllegalArgumentException("Abatimento já estornado");
        }

        for (AbatimentoParcela parcela : abatimento.getParcelas()){
            LancamentoFinanceiro lanc = parcela.getLancamento();

            lanc.setValorPago(lanc.getValorPago().subtract(parcela.getValor()));

            if (lanc.getStatus() == StatusLancamento.PAGO && lanc.getValorPago().compareTo(lanc.getValor()) < 0) {
                lanc.setStatus(statusDeReabertura(lanc));
                lanc.setDataPagamento(null);
            }
            lancamentoRepository.save(lanc);
        }

        abatimento.setEstornado(true);
        abatimento.setEstornadoEm(LocalDate.now());
        abatimentoRepository.save(abatimento);
    }

    /**
     * Registra o recebimento do saldo restante de uma parcela que já foi tocada por abatimento.
     *
     * <p>Existe para fechar um buraco entre o caminho legado de pagamento ({@code pagar()}) e o
     * caminho de abatimento. A invariante do sistema é: parcela com {@code valorPago = 0} tem seu
     * dinheiro contado pelo próprio {@code valor}; parcela com {@code valorPago > 0} tem o dinheiro
     * contado exclusivamente pelos registros de Abatimento. Se o {@code pagar()} apenas marcasse
     * PAGO uma parcela parcialmente abatida, o restante ficaria fora das duas contagens e sumiria
     * do caixa.
     */
    @Transactional
    public void registrarQuitacao(LancamentoFinanceiro lanc, String observacao) {
        BigDecimal restante = lanc.getValor().subtract(lanc.getValorPago());
        if (restante.signum() <= 0) return;

        Abatimento abatimento = Abatimento.builder()
                .cliente(lanc.getCliente())
                .valor(restante)
                .data(LocalDate.now())
                .observacao(observacao)
                .estornado(false)
                .build();

        abatimento.getParcelas().add(AbatimentoParcela.builder()
                .abatimento(abatimento)
                .lancamento(lanc)
                .valor(restante)
                .build());

        abatimentoRepository.save(abatimento);

        lanc.setValorPago(lanc.getValor());
        lancamentoRepository.save(lanc);
    }

    /** Ao reabrir uma parcela quitada, respeita o vencimento: se já passou, volta como VENCIDO.
     *  Sem isso a parcela voltaria sempre como PENDENTE e só seria reclassificada no próximo
     *  ciclo do scheduler de vencidos. */
    private StatusLancamento statusDeReabertura(LancamentoFinanceiro lanc) {
        return lanc.getDataVencimento() != null && lanc.getDataVencimento().isBefore(LocalDate.now())
                ? StatusLancamento.VENCIDO
                : StatusLancamento.PENDENTE;
    }
}
