
package com.example.marluse.relatorios.service;

import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.repository.AbatimentoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.relatorios.dto.*;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.repository.ItemPedidoRepository;
import com.example.marluse.vendas.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RelatoriosService {

    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AbatimentoRepository abatimentoRepository;
    private final PedidoRepository pedidoRepository;
    private final ItemPedidoRepository itemPedidoRepository;

    /** Receita realizada = parcelas quitadas à vista (valorPago = 0) + abatimentos de dívida
     *  recebidos no período. As duas fontes nunca se sobrepõem (invariante do valorPago). */
    private BigDecimal receitaRecebida(LocalDate inicio, LocalDate fim) {
        return lancamentoRepository.somarReceitaPorPeriodo(inicio, fim)
                .add(abatimentoRepository.somarAbatimentosPorPeriodo(inicio, fim));
    }

    // ── KPIs ──────────────────────────────────────────────────────────────────

    public KpisResponse kpis(String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDate inicioPeriodo = resolverInicio(periodo, hoje);

        BigDecimal receita     = receitaRecebida(inicioPeriodo, hoje);
        BigDecimal despesas    = lancamentoRepository.somarDespesaPorPeriodo(inicioPeriodo, hoje);
        BigDecimal cmv         = itemPedidoRepository.somarCmvPorPeriodo(
                inicioPeriodo.atStartOfDay(), hoje.atTime(23, 59, 59));
        BigDecimal saldo       = receita.subtract(despesas);
        BigDecimal lucroLiquido = receita.subtract(cmv).subtract(despesas);
        long totalPedidos      = pedidoRepository.contarVendasPorPeriodo(StatusPedido.PAGO, inicioPeriodo, hoje);
        BigDecimal ticketMedio = totalPedidos > 0
                ? receita.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        LocalDate inicioMesAtual    = hoje.withDayOfMonth(1);
        LocalDate inicioMesAnterior = inicioMesAtual.minusMonths(1);
        LocalDate fimMesAnterior    = inicioMesAtual.minusDays(1);

        BigDecimal receitaMes  = receitaRecebida(inicioMesAtual, hoje);
        BigDecimal despesasMes = lancamentoRepository.somarDespesaPorPeriodo(inicioMesAtual, hoje);
        BigDecimal cmvMes      = itemPedidoRepository.somarCmvPorPeriodo(
                inicioMesAtual.atStartOfDay(), hoje.atTime(23, 59, 59));
        BigDecimal saldoMes    = receitaMes.subtract(despesasMes);
        long pedidosMes        = pedidoRepository.contarVendasPorPeriodo(StatusPedido.PAGO, inicioMesAtual, hoje);
        BigDecimal ticketMes   = pedidosMes > 0
                ? receitaMes.divide(BigDecimal.valueOf(pedidosMes), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal receitaAnt  = receitaRecebida(inicioMesAnterior, fimMesAnterior);
        BigDecimal despesasAnt = lancamentoRepository.somarDespesaPorPeriodo(inicioMesAnterior, fimMesAnterior);
        BigDecimal cmvAnt      = itemPedidoRepository.somarCmvPorPeriodo(
                inicioMesAnterior.atStartOfDay(), fimMesAnterior.atTime(23, 59, 59));
        BigDecimal saldoAnt    = receitaAnt.subtract(despesasAnt);
        long pedidosAnt        = pedidoRepository.contarVendasPorPeriodo(StatusPedido.PAGO, inicioMesAnterior, fimMesAnterior);
        BigDecimal ticketAnt   = pedidosAnt > 0
                ? receitaAnt.divide(BigDecimal.valueOf(pedidosAnt), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new KpisResponse(
                receita, despesas, saldo, ticketMedio, totalPedidos,
                variacao(receitaMes, receitaAnt),
                variacao(despesasMes, despesasAnt),
                variacao(saldoMes, saldoAnt),
                variacao(ticketMes, ticketAnt),
                cmv,
                lucroLiquido
        );
    }

    // ── Receita mensal ────────────────────────────────────────────────────────

    public List<ReceitaMensalItemResponse> receitaMensal(int meses) {
        LocalDate hoje  = LocalDate.now();
        LocalDate inicio = hoje.minusMonths(meses).withDayOfMonth(1);

        // Agregação feita no banco (GROUP BY por mês).
        // Antes: carregava TODOS os lançamentos do período como entidades e agrupava em Java.
        Map<YearMonth, BigDecimal> vendasMap   = agruparPorMes(lancamentoRepository.receitaVendasPorMes(inicio, hoje));
        Map<YearMonth, BigDecimal> locacoesMap = agruparPorMes(lancamentoRepository.receitaLocacoesPorMes(inicio, hoje));
        Map<YearMonth, BigDecimal> despesasMap = agruparPorMes(lancamentoRepository.despesaPorMes(inicio, hoje));

        // CMV por mês — agrupa resultado da query em mapa YearMonth → valor
        Map<YearMonth, BigDecimal> cmvMap = agruparPorMes(
                itemPedidoRepository.cmvPorMes(inicio.atStartOfDay(), hoje.atTime(23, 59, 59)));

        List<ReceitaMensalItemResponse> result = new ArrayList<>();
        YearMonth ym = YearMonth.from(inicio);
        YearMonth fim = YearMonth.from(hoje);
        while (!ym.isAfter(fim)) {
            result.add(new ReceitaMensalItemResponse(
                    ym.toString(),
                    vendasMap.getOrDefault(ym, BigDecimal.ZERO),
                    locacoesMap.getOrDefault(ym, BigDecimal.ZERO),
                    despesasMap.getOrDefault(ym, BigDecimal.ZERO),
                    cmvMap.getOrDefault(ym, BigDecimal.ZERO)
            ));
            ym = ym.plusMonths(1);
        }
        return result;
    }

    // ── Status financeiro ─────────────────────────────────────────────────────

    public StatusFinanceiroResponse statusFinanceiro() {
        LocalDate hoje    = LocalDate.now();
        long pagos        = lancamentoRepository.countByStatus(StatusLancamento.PAGO);
        long vencidos     = lancamentoRepository.countByStatusAndDataVencimentoBefore(StatusLancamento.PENDENTE, hoje);
        long totalPend    = lancamentoRepository.countByStatus(StatusLancamento.PENDENTE);
        long pendentes    = totalPend - vencidos;
        return new StatusFinanceiroResponse(pagos, pendentes, vencidos);
    }

    // ── Top clientes ──────────────────────────────────────────────────────────

    public List<TopClienteResponse> topClientes(int limite, String periodo) {
        LocalDate inicio = resolverInicio(periodo, LocalDate.now());
        List<Object[]> rows = lancamentoRepository.topClientesPorReceita(
                inicio, LocalDate.now(), PageRequest.of(0, limite));
        return rows.stream()
                .map(r -> new TopClienteResponse((String) r[0], (BigDecimal) r[1]))
                .toList();
    }

    // ── Top produtos ──────────────────────────────────────────────────────────

    public List<TopProdutoResponse> topProdutos(int limite, String periodo) {
        LocalDate inicio = resolverInicio(periodo, LocalDate.now());
        LocalDate fim    = LocalDate.now();
        List<Object[]> rows = itemPedidoRepository.topProdutos(
                inicio.atStartOfDay(), fim.atTime(23, 59, 59), PageRequest.of(0, limite));
        return rows.stream()
                .map(r -> {
                    BigDecimal total = (BigDecimal) r[2];
                    BigDecimal custo = (BigDecimal) r[3];
                    BigDecimal lucro = total.subtract(custo);
                    return new TopProdutoResponse(
                            (String) r[0],
                            (BigDecimal) r[1],
                            lucro,
                            custo,
                            total
                    );
                })
                .toList();
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private LocalDate resolverInicio(String periodo, LocalDate hoje) {
        return switch (periodo != null ? periodo : "6m") {
            case "3m"  -> hoje.minusMonths(3);
            case "12m" -> hoje.minusMonths(12);
            default    -> hoje.minusMonths(6);
        };
    }

    private Double variacao(BigDecimal atual, BigDecimal anterior) {
        if (anterior.compareTo(BigDecimal.ZERO) == 0) return null;
        return atual.subtract(anterior)
                .divide(anterior, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();
    }

    /** Converte linhas [ '%Y-%m', SUM(valor) ] das queries agregadas em mapa YearMonth → valor. */
    private static Map<YearMonth, BigDecimal> agruparPorMes(List<Object[]> rows) {
        return rows.stream().collect(Collectors.toMap(
                r -> YearMonth.parse((String) r[0]),
                r -> (BigDecimal) r[1]
        ));
    }
}
