package com.example.marluse.clientes.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ClienteSaldoResponse(
        BigDecimal saldoDevedor,
        List<ItemDevido> itens
) {
    public record ItemDevido(
            String origemTipo,   // "PEDIDO", "LOCACAO" ou "LANCAMENTO" (receita avulsa com cliente)
            String origemId,
            Long numero,
            LocalDate data,
            BigDecimal valorTotal,
            BigDecimal valorPago,
            BigDecimal saldo
    ){}
}
