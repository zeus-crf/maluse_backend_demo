package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.model.Abatimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AbatimentoResultado(
        String abatimentoId,
        BigDecimal valorAbatido,
        BigDecimal saldoAnterior,
        BigDecimal saldoAtual,
        LocalDate data
) {
    public static AbatimentoResultado from(Abatimento a, BigDecimal saldoAnterior, BigDecimal saldoAtual) {
        return new AbatimentoResultado(a.getId(), a.getValor(), saldoAnterior, saldoAtual, a.getData());
    }
}
