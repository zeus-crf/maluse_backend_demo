package com.example.marluse.clientes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LocacaoResumo(
        String id,
        Long numero,
        String status,
        String formaPagamento,
        BigDecimal valorTotal,
        LocalDate dataRetirada,
        LocalDate dataDevolucaoPrevista
) {
}
