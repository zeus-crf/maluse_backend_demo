package com.example.marluse.relatorios.dto;

import java.math.BigDecimal;

public record TopClienteResponse(
        String nome,
        BigDecimal total
) {}
