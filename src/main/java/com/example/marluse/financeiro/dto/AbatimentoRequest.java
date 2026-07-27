package com.example.marluse.financeiro.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AbatimentoRequest(
        @NotNull(message = "Valor é obrigatório")
        @DecimalMin(value = "0.0", inclusive = false, message = "O valor deve ser maior que zero")
        BigDecimal valor,
        String observacao
) {}