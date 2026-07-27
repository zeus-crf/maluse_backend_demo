package com.example.marluse.dashboard.dto;

import java.math.BigDecimal;

public record DashboardKpisResponse(
        BigDecimal receitaPeriodo,
        BigDecimal vendasValor,
        long vendasQuantidade,
        long locacoesAtivas,
        BigDecimal locacoesAtivasValor,
        long clientesAtivos
) {
}
