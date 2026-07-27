package com.example.marluse.estoque.dto;

import com.example.marluse.estoque.enums.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record ProdutoRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String descricao,

        @NotNull(message = "O valor de compra é obrigatorio")
        @DecimalMin(value = "0.0", inclusive = false, message = "O Preço deve ser maior que 0")
        BigDecimal valorCompra,


        @DecimalMin(value = "0.0", inclusive = false, message = "Preço deve ser maior que zero")
        BigDecimal preco,

        @DecimalMin(value = "0.0", inclusive = false, message = "Preço da diária deve ser maior que zero")
        BigDecimal precoDiaria,

        @DecimalMin(value = "0", message = "Quantidade não pode ser negativa")
        BigDecimal quantidadeEstoque,

        @Min(value = 0, message = "Estoque mínimo não pode ser negativo")
        Integer estoqueMinimo,

        @NotNull(message = "Unidade de medida é obrigatória")
        UnidadeMedida medida,

        @NotNull(message = "Categoria é obrigatória")
        CategoriaProduto categoria,

        List<ProdutoFornecedorRequest> fornecedores
) {
}
