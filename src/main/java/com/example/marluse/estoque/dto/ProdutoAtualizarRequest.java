package com.example.marluse.estoque.dto;

import com.example.marluse.estoque.enums.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoAtualizarRequest(

        String nome,
        String descricao,
        BigDecimal valorCompra,
        BigDecimal preco,
        BigDecimal precoDiaria,
        BigDecimal quantidadeEstoque,
        Integer estoqueMinimo,
        UnidadeMedida medida,
        CategoriaProduto categoria,
        List<ProdutoFornecedorRequest> fornecedores
) {
}
