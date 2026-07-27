package com.example.marluse.locacoes.dto;

import com.example.marluse.entrega.dto.EntregaRequest;
import com.example.marluse.entrega.model.Entrega;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record LocacaoRequest(
        String clienteId,

        @NotNull(message = "Forma de pagamento é obrigatória")
        FormaPagamento formaPagamento,

        @NotNull(message = "Data de retirada é obrigatória")
        LocalDate dataRetirada,

        @NotNull(message = "Data de devolução prevista é obrigatória")
        LocalDate dataDevolucaoPrevista,

        @NotEmpty(message = "Locação deve ter ao menos um item")
        List<ItemLocacaoRequest> itens,

        String observacao,

        /** Opcional — quando não informado, padrão é ATIVA */
        StatusLocacao status,

        BigDecimal desconto,

        TipoDesconto tipoDesconto,

        Integer numeroParcelas,

        LocalDate primeiroVencimento,

        EntregaRequest entrega,

        BigDecimal juros,

        TipoDesconto tipoJuros,

        LocalDate dataMovimento

) {
}
