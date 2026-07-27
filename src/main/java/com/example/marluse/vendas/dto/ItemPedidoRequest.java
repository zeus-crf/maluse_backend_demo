package com.example.marluse.vendas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ItemPedidoRequest(

        String productId,

        String produtoNome,

        @NotNull(message = "Quantidade é obrigatória")
        @DecimalMin(value = "0", inclusive = false, message = "A Quantidade deve ser maior que zero")
        BigDecimal quantidade,

        BigDecimal preco,

        boolean baixarEstoque,

        boolean permitirSemEstoque
) {

    public boolean isProdutoNovo() {
        return  (productId == null || productId.isBlank())
                    && produtoNome != null && !produtoNome.isBlank();
    }
}
