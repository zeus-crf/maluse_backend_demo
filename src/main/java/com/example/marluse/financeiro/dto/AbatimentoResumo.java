package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.model.Abatimento;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AbatimentoResumo(
        String id,
        BigDecimal valor,
        LocalDate data,
        String observacao,
        boolean estornado
) {
    public static AbatimentoResumo from(Abatimento a) {
        return new AbatimentoResumo(a.getId(), a.getValor(), a.getData(), a.getObservacao(), a.isEstornado());
    }
}
