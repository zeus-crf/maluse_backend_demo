package com.example.marluse.vendas.dto;

import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoAtualizarRequest(

        FormaPagamento formaPagamento,

        String observacao,

        BigDecimal desconto,

        TipoDesconto tipoDesconto,

        BigDecimal juros,

        TipoDesconto tipoJuros,

        LocalDate dataMovimento
) {}
