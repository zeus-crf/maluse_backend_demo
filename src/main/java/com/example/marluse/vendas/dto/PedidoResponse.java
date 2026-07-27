package com.example.marluse.vendas.dto;

import com.example.marluse.entrega.dto.EntregaResponse;
import com.example.marluse.financeiro.dto.ParcelaResponse;
import com.example.marluse.vendas.enums.TipoDesconto;
import com.example.marluse.vendas.model.Pedido;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PedidoResponse(
        String id,
        Long numero,
        String clienteId,
        String clienteNome,
        String clienteTelefone,
        StatusPedido status,
        FormaPagamento formaPagamento,
        BigDecimal valorTotal,
        String observacao,
        List<ItemPedidoResponse> itens,
        LocalDateTime createdAt,
        LocalDate dataVencimento,
        BigDecimal valorBruto,
        BigDecimal desconto,
        TipoDesconto tipoDesconto,
        LocalDate descontoAplicadoEm,
        List<ParcelaResponse> parcelas,
        ParcelaResponse parcelaMesAtual,
        EntregaResponse entrega,
        BigDecimal juros,
        TipoDesconto tipoJuros,
        LocalDate dataMovimento,
        LocalDate jurosAplicadoEm
) {

    public static PedidoResponse from(Pedido pedido) {
        return from(pedido, null, null);
    }

    public static PedidoResponse from(Pedido pedido, List<ParcelaResponse> parcelas) {
        return from(pedido, parcelas, null);
    }


    public static PedidoResponse from(Pedido pedido, List<ParcelaResponse> parcelas, ParcelaResponse parcelaMesAtual) {
       BigDecimal bruto = pedido.getItens().stream()
               .map(i -> i.getPrecoUnitario().multiply(i.getQuantidade()))
               .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PedidoResponse(
                pedido.getId(),
                pedido.getNumero(),
                pedido.getCliente() != null ? pedido.getCliente().getId() : null,
                pedido.getCliente() != null ? pedido.getCliente().getNome() : "Consumidor Final",
                pedido.getCliente() != null ? pedido.getCliente().getTelefone() : null,
                statusEfetivo(pedido),
                pedido.getFormaPagamento(),
                pedido.getValorTotal(),
                pedido.getObservacao(),
                pedido.getItens().stream().map(ItemPedidoResponse::from).toList(),
                pedido.getCreatedAt(),
                pedido.getDataVencimento(),
                bruto,
                pedido.getDesconto(),
                pedido.getTipoDesconto(),
                pedido.getDescontoAplicadoEm(),
                parcelas,
                parcelaMesAtual,
                pedido.getEntrega() != null ? new EntregaResponse(
                        pedido.getEntrega().getId(),
                        pedido.getEntrega().getEndereco(),
                        pedido.getEntrega().getDataPrevista(),
                        pedido.getEntrega().getDataEntrega(),
                        pedido.getEntrega().getStatus()
                ) : null,
                pedido.getJuros(),
                pedido.getTipoJuros(),
                pedido.getDataMovimento() != null ? pedido.getDataMovimento() : null,
                pedido.getJurosAplicadoEm()
        );
    }

    private static StatusPedido statusEfetivo(Pedido pedido) {
        if (pedido.getStatus() == StatusPedido.CONFIRMADO
                && pedido.getFormaPagamento() == FormaPagamento.FIADO
                && pedido.getDataVencimento() != null
                && pedido.getDataVencimento().isBefore(LocalDate.now())) {
            return StatusPedido.PENDENTE;
        }
        return pedido.getStatus();
    }
}