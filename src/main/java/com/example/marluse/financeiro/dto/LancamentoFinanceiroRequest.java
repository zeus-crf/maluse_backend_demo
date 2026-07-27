package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.enums.Recorrencia;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoFinanceiroRequest(
        @NotNull TipoLancamento tipo,
        @NotBlank String categoria,
        @NotBlank String descricao,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        @NotNull StatusLancamento status,
        Recorrencia recorrencia,
        LocalDate dataPagamento,
        LocalDate dataVencimento,
        String clienteId
) {}
