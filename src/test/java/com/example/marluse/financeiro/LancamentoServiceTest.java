package com.example.marluse.financeiro;

import com.example.marluse.financeiro.dto.LancamentoFinanceiroRequest;
import com.example.marluse.financeiro.dto.LancamentoFinanceiroResponse;
import com.example.marluse.financeiro.dto.ResumoDiaResponse;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.financeiro.service.LancamentoFinanceiroService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class LancamentoServiceTest {

    @Autowired
    private LancamentoFinanceiroService lancamentoService;

    @Autowired
    private LancamentoFinanceiroRepository lancamentoRepository;



    @BeforeEach
    void setUp(){
        lancamentoRepository.deleteAll();
    }

    private LancamentoFinanceiroRequest buildRequest(TipoLancamento tipo, BigDecimal valor,
                                                     StatusLancamento status, LocalDate vencimento) {
        return new LancamentoFinanceiroRequest(
                tipo,
                "Teste",
                "Descrição de teste",
                valor,
                status,
                null,   // recorrencia
                status == StatusLancamento.PAGO ? LocalDate.now() : null,
                vencimento,
                null    // clienteId
        );
    }

    @Test
    void deveCriarLancamento() {
        LancamentoFinanceiroRequest request = buildRequest(
                TipoLancamento.RECEITA, new BigDecimal("500.00"),
                StatusLancamento.PENDENTE, LocalDate.now().plusDays(10));

        LancamentoFinanceiroResponse response = lancamentoService.criar(request);

        assertNotNull(response.id());
        assertEquals(TipoLancamento.RECEITA, response.tipo());
        assertEquals(new BigDecimal("500.00"), response.valor());
        assertEquals(StatusLancamento.PENDENTE, response.status());
    }

    @Test
    void deveListarLancamentos() {
        lancamentoService.criar(buildRequest(TipoLancamento.RECEITA, new BigDecimal("100.00"),
                StatusLancamento.PAGO, LocalDate.now()));
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("50.00"),
                StatusLancamento.PENDENTE, LocalDate.now().plusDays(5)));

        List<LancamentoFinanceiroResponse> lista = lancamentoService.listarTodos();

        assertEquals(2, lista.size());
    }

    @Test
    void deveListarPendentes(){
        lancamentoService.criar(buildRequest(TipoLancamento.RECEITA, new BigDecimal("100.00"),
                StatusLancamento.PAGO, LocalDate.now()));
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("50.00"),
                StatusLancamento.PENDENTE, LocalDate.now().plusDays(5)));
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("50.00"),
                StatusLancamento.PENDENTE, LocalDate.now().plusDays(5)));

        List<LancamentoFinanceiroResponse> lista = lancamentoService.listarPendentes();

        assertEquals(2, lista.size());

    }

    @Test
    void deveListarVencidos() {
        // Vencido (data passada + pendente)
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("80.00"),
                StatusLancamento.PENDENTE, LocalDate.now().minusDays(3)));
        // Não vencido
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("80.00"),
                StatusLancamento.PENDENTE, LocalDate.now().plusDays(5)));

        List<LancamentoFinanceiroResponse> vencidos = lancamentoService.listarVencidos();

        assertEquals(1, vencidos.size());
    }

    @Test
    void devePagarLancamento() {
        LancamentoFinanceiroResponse criado = lancamentoService.criar(
                buildRequest(TipoLancamento.RECEITA, new BigDecimal("300.00"),
                        StatusLancamento.PENDENTE, LocalDate.now().plusDays(30)));

        LancamentoFinanceiroResponse pago = lancamentoService.pagar(criado.id());

        assertEquals(StatusLancamento.PAGO, pago.status());
        assertNotNull(pago.dataPagamento());
        assertEquals(LocalDate.now(), pago.dataPagamento());
    }

    @Test
    void deveLancarExcecaoAoPagarLancamentoJaPago() {
        LancamentoFinanceiroResponse criado = lancamentoService.criar(
                buildRequest(TipoLancamento.RECEITA, new BigDecimal("300.00"),
                        StatusLancamento.PAGO, LocalDate.now()));

        assertThrows(IllegalArgumentException.class, () -> lancamentoService.pagar(criado.id()));
    }



    @Test
    void deveCalcularResumoDia() {
        lancamentoService.criar(buildRequest(TipoLancamento.RECEITA, new BigDecimal("1000.00"),
                StatusLancamento.PAGO, LocalDate.now()));
        lancamentoService.criar(buildRequest(TipoLancamento.RECEITA, new BigDecimal("500.00"),
                StatusLancamento.PAGO, LocalDate.now()));
        lancamentoService.criar(buildRequest(TipoLancamento.DESPESA, new BigDecimal("300.00"),
                StatusLancamento.PAGO, LocalDate.now()));

        ResumoDiaResponse resumo = lancamentoService.resumoDia();

        assertEquals(new BigDecimal("1500.00"), resumo.totalReceitas());
        assertEquals(new BigDecimal("300.00"), resumo.totalDespesas());
        assertEquals(new BigDecimal("1200.00"), resumo.saldo());
    }

    @Test
    void deveDeletarLancamento() {
        LancamentoFinanceiroResponse criado = lancamentoService.criar(
                buildRequest(TipoLancamento.DESPESA, new BigDecimal("100.00"),
                        StatusLancamento.PENDENTE, LocalDate.now().plusDays(5)));

        lancamentoService.deletar(criado.id());

        assertEquals(0, lancamentoRepository.count());
    }

}
