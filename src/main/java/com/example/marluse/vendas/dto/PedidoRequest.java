package com.example.marluse.vendas.dto;

import com.example.marluse.entrega.dto.EntregaRequest;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record PedidoRequest(

        String clienteId,

        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @NotEmpty(message = "Pedido deve ter ao menos um item")
        List<ItemPedidoRequest> itens,

        String observacao,

        StatusPedido status,

        LocalDate dataVencimento,

        BigDecimal desconto,

        TipoDesconto tipoDesconto,

        Integer numeroParcelas,

        LocalDate primeiroVencimento,

        EntregaRequest entrega,

        BigDecimal juros,

        TipoDesconto tipoJuros,

        LocalDate dataMovimento

) {}
