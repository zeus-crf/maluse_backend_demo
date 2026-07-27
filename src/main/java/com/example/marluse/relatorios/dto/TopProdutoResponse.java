package com.example.marluse.relatorios.dto;

import java.math.BigDecimal;

public record TopProdutoResponse(
        String nome,
        BigDecimal quantidade,
        BigDecimal lucro,
        BigDecimal custo,
        BigDecimal total
) {}
