package com.example.marluse.vendas.dto;

import com.example.marluse.vendas.model.ItemPedido;

import java.math.BigDecimal;

public record ItemPedidoResponse(
        String id,
        String produtoId,
        String produtoNome,
        BigDecimal quantidade,
        BigDecimal precoUnitario,
        BigDecimal subtotal,
        boolean baixarEstoque,
        boolean permitirSemEstoque
) {
    public static ItemPedidoResponse from(ItemPedido item) {
        return new ItemPedidoResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoUnitario(),
                item.getSubTotal(),
                item.isBaixar_estoque(),
                item.isPermitirSemEstoque()
        );
    }
}