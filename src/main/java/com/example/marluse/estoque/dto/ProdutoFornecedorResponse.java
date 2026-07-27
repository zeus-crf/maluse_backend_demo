package com.example.marluse.estoque.dto;

import com.example.marluse.estoque.model.ProdutoFornecedor;

import java.math.BigDecimal;

public record ProdutoFornecedorResponse(
        String nome,
        BigDecimal precoCompra
) {
    public static ProdutoFornecedorResponse from(ProdutoFornecedor pf) {
        return new ProdutoFornecedorResponse(pf.getFornecedor().getNome(), pf.getPrecoCompra());
    }
}
