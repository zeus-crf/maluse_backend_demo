package com.example.marluse.locacoes.dto;

import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LocacaoEdicaoRequest(

        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        String observacao,

        BigDecimal desconto,

        TipoDesconto tipoDesconto,

        BigDecimal juros,

        TipoDesconto tipoJuros,

        LocalDate dataMovimento

) {}
