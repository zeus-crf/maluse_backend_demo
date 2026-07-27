package com.example.marluse.relatorios.dto;

public record StatusFinanceiroResponse(
        long pagos,
        long pendentes,
        long vencidos
) {}
