package com.example.marluse.financeiro.dto;

import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LancamentoAtualizarRequest(
        TipoLancamento tipo,
        String categoria,
        String descricao,
        @DecimalMin("0.01") BigDecimal valor,
        StatusLancamento status,
        LocalDate dataVencimento,
        LocalDate dataPagamento
) {}
