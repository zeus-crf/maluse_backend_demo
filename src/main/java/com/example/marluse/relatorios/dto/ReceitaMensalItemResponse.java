package com.example.marluse.relatorios.dto;

import java.math.BigDecimal;

public record ReceitaMensalItemResponse(
        String mes,
        BigDecimal vendas,
        BigDecimal locacoes,
        BigDecimal despesas,
        BigDecimal cmv
) {}
