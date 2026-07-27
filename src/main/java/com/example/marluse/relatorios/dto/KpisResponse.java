package com.example.marluse.relatorios.dto;

import java.math.BigDecimal;

public record KpisResponse(
        BigDecimal receita,
        BigDecimal despesas,
        BigDecimal saldo,
        BigDecimal ticketMedio,
        long totalPedidos,
        Double variacaoReceita,
        Double variacaoDespesas,
        Double variacaoSaldo,
        Double variacaoTicketMedio,
        BigDecimal cmv,
        BigDecimal lucroLiquido
) {}
