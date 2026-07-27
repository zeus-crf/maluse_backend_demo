package com.example.marluse.financeiro.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ResumoDiaResponse(
        LocalDate data,
        BigDecimal totalReceitas,
        BigDecimal totalDespesas,
        BigDecimal saldo
) {}
