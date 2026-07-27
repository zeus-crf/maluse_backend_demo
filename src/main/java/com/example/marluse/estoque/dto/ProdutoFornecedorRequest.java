package com.example.marluse.estoque.dto;

import java.math.BigDecimal;

public record ProdutoFornecedorRequest(
        String nome,
        BigDecimal precoCompra
) {
}
