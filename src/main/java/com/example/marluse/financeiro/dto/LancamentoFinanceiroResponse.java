package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.enums.Recorrencia;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record LancamentoFinanceiroResponse(
        String id,
        TipoLancamento tipo,
        String categoria,
        String descricao,
        BigDecimal valor,
        LocalDate dataVencimento,
        LocalDate dataPagamento,
        StatusLancamento status,
        String clienteId,
        Recorrencia recorrencia,
        String recorrenciaGrupoId,
        boolean recorrenciaAtiva,
        String clienteNome,
        Integer numParcelas,
        Integer totalParcelas,
        String pedidoId,
        String locacaoId,
        LocalDateTime createdAt
) {
    public static LancamentoFinanceiroResponse from(LancamentoFinanceiro l){
        return new LancamentoFinanceiroResponse(
                l.getId(),
                l.getTipo(),
                l.getCategoria(),
                l.getDescricao(),
                l.getValor(),
                l.getDataVencimento(),
                l.getDataPagamento(),
                l.getStatus(),
                l.getCliente() != null ? l.getCliente().getId() : null,
                l.getRecorrencia(),
                l.getRecorrenciaGrupoId(),
                Boolean.TRUE.equals(l.getRecorrenciaAtiva()),
                l.getCliente() != null ? l.getCliente().getNome() : null,
                l.getNumParcelas(),
                l.getTotalParcelas(),
                l.getPedido() != null ? l.getPedido().getId() : null,
                l.getLocacao() != null ? l.getLocacao().getId() : null,
                l.getCreatedAt()
        );
    }
}
