package com.example.marluse.estoque.dto;

import com.example.marluse.estoque.model.Fornecedor;

public record FornecedorResponse(
        String id,
        String nome
) {

    public static FornecedorResponse from(Fornecedor fornecedor){
        return new FornecedorResponse(fornecedor.getId(), fornecedor.getNome());
    }
}
