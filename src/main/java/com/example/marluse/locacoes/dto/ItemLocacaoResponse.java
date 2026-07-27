package com.example.marluse.locacoes.dto;

import com.example.marluse.locacoes.model.ItemLocacao;

import java.math.BigDecimal;

public record ItemLocacaoResponse(
        String id,
        String produtoId,
        String produtoNome,
        BigDecimal quantidade,
        BigDecimal precoDiaria,
        BigDecimal subtotal,
        boolean baixarEstoque,
        boolean permitirSemEstoque
) {
    public static ItemLocacaoResponse from(ItemLocacao item) {
        return new ItemLocacaoResponse(
                item.getId(),
                item.getProduto().getId(),
                item.getProduto().getNome(),
                item.getQuantidade(),
                item.getPrecoDiaria(),
                item.getSubtotal(),
                item.isBaixar_estoque(),
                item.isPermitirSemEstoque()
        );
    }
}