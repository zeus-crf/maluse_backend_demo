package com.example.marluse.clientes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoResumo(
        String id,
        Long numero,
        String status,
        String formaPagamento,
        BigDecimal valorTotal,
        LocalDate dataMovimento
) {
}
