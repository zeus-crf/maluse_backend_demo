package com.example.marluse.financeiro.repository;

import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface LancamentoFinanceiroRepository extends JpaRepository<LancamentoFinanceiro, String> {

    List<LancamentoFinanceiro> findByStatus(StatusLancamento status);

    List<LancamentoFinanceiro> findByPedidoId(String pedidoId);

    List<LancamentoFinanceiro> findByLocacaoId(String locacaoId);

    List<LancamentoFinanceiro> findByPedidoIdAndStatusNot(String pedidoId, StatusLancamento status);

    List<LancamentoFinanceiro> findByLocacaoIdAndStatusNot(String locacaoId, StatusLancamento status);

    void deleteByLocacaoId(String locacaoId);

    List<LancamentoFinanceiro> findByDataVencimentoBetween(LocalDate inicio, LocalDate fim);

    /** Lançamentos com valorPago > 0 foram quitados via abatimento: o dinheiro deles é contado
     *  pelos registros de Abatimento, nunca pelo valor da parcela (evita dupla contagem). */
    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l WHERE l.tipo = :tipo AND l.status = 'PAGO' AND l.valorPago = 0 AND l.dataPagamento = :data")
    BigDecimal somarPorTipoEData(TipoLancamento tipo, LocalDate data);

    @Query("SELECT l FROM LancamentoFinanceiro l WHERE l.status = 'PENDENTE' AND l.dataVencimento < :hoje")
    List<LancamentoFinanceiro> findVencidos(LocalDate hoje);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l WHERE l.tipo = 'RECEITA' AND l.status = 'PAGO' AND l.valorPago = 0 AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarReceitaPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l " +
            "WHERE l.pedido IS NOT NULL AND l.status = 'PAGO' AND l.valorPago = 0 " +
            "AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarReceitaVendasPorPagamento(@Param("inicio") LocalDate inicio,
                                              @Param("fim") LocalDate fim);

    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l WHERE l.tipo = 'DESPESA' AND l.status = 'PAGO' AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarDespesaPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    // ── Agregações mensais para relatórios (GROUP BY no banco, sem carregar entidades) ──
    // Retornam pares [ '%Y-%m', SUM(valor) ].

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m'), COALESCE(SUM(l.valor), 0)
        FROM LancamentoFinanceiro l
        WHERE l.tipo = 'RECEITA' AND l.status = 'PAGO' AND l.pedido IS NOT NULL
        AND l.dataPagamento BETWEEN :inicio AND :fim
        GROUP BY FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m')
    """)
    List<Object[]> receitaVendasPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m'), COALESCE(SUM(l.valor), 0)
        FROM LancamentoFinanceiro l
        WHERE l.tipo = 'RECEITA' AND l.status = 'PAGO' AND l.locacao IS NOT NULL
        AND l.dataPagamento BETWEEN :inicio AND :fim
        GROUP BY FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m')
    """)
    List<Object[]> receitaLocacoesPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
        SELECT FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m'), COALESCE(SUM(l.valor), 0)
        FROM LancamentoFinanceiro l
        WHERE l.tipo = 'DESPESA' AND l.status = 'PAGO'
        AND l.dataPagamento BETWEEN :inicio AND :fim
        GROUP BY FUNCTION('DATE_FORMAT', l.dataPagamento, '%Y-%m')
    """)
    List<Object[]> despesaPorMes(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("""
    SELECT l FROM LancamentoFinanceiro l
    WHERE l.recorrencia IS NOT NULL
    AND l.recorrenciaAtiva = true
    AND l.dataVencimento = (
        SELECT MAX(l2.dataVencimento)
        FROM LancamentoFinanceiro l2
        WHERE l2.recorrenciaGrupoId = l.recorrenciaGrupoId
    )
    """)
    List<LancamentoFinanceiro> findUltimosPorGrupoAtivo();

    List<LancamentoFinanceiro> findByRecorrenciaGrupoId(String recorrenciaGrupoId);

    List<LancamentoFinanceiro> findByTipoAndStatusAndDataPagamentoBetween(
            TipoLancamento tipo, StatusLancamento status, LocalDate inicio, LocalDate fim);

    long countByStatus(StatusLancamento status);

    long countByStatusAndDataVencimentoBefore(StatusLancamento status, LocalDate data);

    @Query("""
        SELECT l.cliente.nome, SUM(l.valor)
        FROM LancamentoFinanceiro l
        WHERE l.tipo = 'RECEITA' AND l.status = 'PAGO'
        AND l.cliente IS NOT NULL
        AND l.dataPagamento BETWEEN :inicio AND :fim
        GROUP BY l.cliente.id, l.cliente.nome
        ORDER BY SUM(l.valor) DESC
    """)
    List<Object[]> topClientesPorReceita(@Param("inicio") LocalDate inicio,
                                         @Param("fim") LocalDate fim,
                                         Pageable pageable);

    @Query("SELECT l FROM LancamentoFinanceiro l " +
            "WHERE l.pedido IS NOT NULL " +
            "AND l.totalParcelas > 1 " +
            "AND l.status = 'PENDENTE' " +
            "ORDER BY l.dataVencimento ASC NULLS LAST")
    List<LancamentoFinanceiro> findProximasParcelasPendentes();

    @Query("""
        SELECT l FROM LancamentoFinanceiro l
        WHERE l.pedido IS NOT NULL
        AND l.totalParcelas > 1
        AND l.status = 'PAGO'
        AND NOT EXISTS (
            SELECT 1 FROM LancamentoFinanceiro l2
            WHERE l2.pedido = l.pedido
            AND l2.status = 'PENDENTE'
        )
        ORDER BY l.numParcelas DESC NULLS LAST
    """)
    List<LancamentoFinanceiro> findUltimasParcelasDePedidosPagos();

    @Query("SELECT l FROM LancamentoFinanceiro l JOIN FETCH l.locacao " +
            "WHERE l.totalParcelas > 1 " +
            "AND l.status = 'PENDENTE' " +
            "ORDER BY l.dataVencimento ASC NULLS LAST")
    List<LancamentoFinanceiro> findProximasParcelasPendentesLocacoes();

    @Query("""
        SELECT l FROM LancamentoFinanceiro l JOIN FETCH l.locacao
        WHERE l.totalParcelas > 1
        AND l.status = 'PAGO'
        AND NOT EXISTS (
            SELECT 1 FROM LancamentoFinanceiro l2
            WHERE l2.locacao = l.locacao
            AND l2.status = 'PENDENTE'
        )
        ORDER BY l.numParcelas DESC NULLS LAST
    """)
    List<LancamentoFinanceiro> findUltimasParcelasDeLocacoesPagas();

    /** Soma somente lançamentos de locações (locacao IS NOT NULL) pagos no período — usa dataPagamento */
    @Query("SELECT COALESCE(SUM(l.valor), 0) FROM LancamentoFinanceiro l " +
           "WHERE l.locacao IS NOT NULL AND l.status = 'PAGO' AND l.valorPago = 0 " +
           "AND l.dataPagamento BETWEEN :inicio AND :fim")
    BigDecimal somarReceitaLocacoesPorPagamento(@Param("inicio") LocalDate inicio,
                                                @Param("fim") LocalDate fim);


    /** Lançamentos RECEITA em aberto de um cliente, ordenados do mais antigo para o mais recente.
     *
     *  <p>Três critérios, nesta ordem:
     *  <ol>
     *    <li><b>Data da origem</b> — do pedido/locação. Para receitas avulsas (sem pedido nem
     *        locação, lançadas direto no financeiro) cai no vencimento do próprio lançamento; sem
     *        esse fallback elas viriam com data NULL e o MySQL as ordenaria à frente de tudo.</li>
     *    <li><b>Id da origem</b> — mantém as parcelas de um mesmo pedido/locação juntas. Sem isso,
     *        pedidos de mesma data se intercalam e o abatimento paga a 1ª parcela de todos antes de
     *        voltar para a 2ª, em vez de quitar um pedido por vez.</li>
     *    <li><b>Número da parcela</b> — dentro da origem, da primeira para a última.</li>
     *  </ol> */
    @Query("SELECT l FROM LancamentoFinanceiro l " +
            "LEFT JOIN l.pedido pe LEFT JOIN l.locacao lo " +
            "WHERE l.cliente.id = :clienteId AND l.tipo = 'RECEITA' " +
            "AND l.status IN ('PENDENTE','VENCIDO') " +
            "AND l.valorPago < l.valor " +
            "ORDER BY COALESCE(pe.dataMovimento, lo.dataMovimento, l.dataVencimento) ASC, " +
            "COALESCE(pe.id, lo.id, l.id) ASC, " +
            "COALESCE(l.numParcelas, 1) ASC")
    List<LancamentoFinanceiro> findEmAbertoPorClienteFifo(@Param("clienteId") String clienteId);

    /** Saldo devedor total do cliente = soma de (valor - valorPago) dos lançamentos em aberto. */
    @Query("SELECT COALESCE(SUM(l.valor - l.valorPago), 0) FROM LancamentoFinanceiro l " +
            "WHERE l.cliente.id = :clienteId AND l.tipo = 'RECEITA' " +
            "AND l.status IN ('PENDENTE','VENCIDO')")
    BigDecimal saldoDevedorPorCliente(@Param("clienteId") String clienteId);

}
