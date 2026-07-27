package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParcelaResponse(
        String id,
        int numeroParcela,
        int totalParcelas,
        BigDecimal valor,
        LocalDate dataVencimento,
        StatusLancamento status,
        LocalDate dataPagamento
) {
    public static ParcelaResponse from(LancamentoFinanceiro l) {
        return new ParcelaResponse(
                l.getId(),
                l.getNumParcelas()    != null ? l.getNumParcelas()    : 1,
                l.getTotalParcelas()  != null ? l.getTotalParcelas()  : 1,
                l.getValor(),
                l.getDataVencimento(),
                l.getStatus(),
                l.getDataPagamento()
        );
    }
}
