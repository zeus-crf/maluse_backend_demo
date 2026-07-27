package com.example.marluse.dashboard.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record GraficoItemResponse(
        LocalDate dia,
        BigDecimal vendas,
        BigDecimal locacoes
){
}
