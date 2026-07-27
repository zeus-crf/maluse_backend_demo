package com.example.marluse.locacoes.dto;

import com.example.marluse.entrega.dto.EntregaResponse;
import com.example.marluse.financeiro.dto.ParcelaResponse;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record LocacaoResponse(
        String id,
        Long numero,
        String clienteId,
        String clienteNome,
        String clienteTelefone,
        StatusLocacao status,
        FormaPagamento formaPagamento,
        LocalDate dataRetirada,
        LocalDate dataDevolucaoPrevista,
        LocalDate dataDevolucaoReal,
        BigDecimal valorTotal,
        String observacao,
        List<ItemLocacaoResponse> itens,
        LocalDateTime createdAt,
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
    public static LocacaoResponse from(Locacao locacao) {
        return from(locacao, null, null);
    }

    public static LocacaoResponse from(Locacao locacao, List<ParcelaResponse> parcelas) {
        return from(locacao, parcelas, null);
    }

    public static LocacaoResponse from(Locacao locacao, List<ParcelaResponse> parcelas, ParcelaResponse parcelaMesAtual) {
        BigDecimal bruto = locacao.getItens().stream()
                .map(i -> i.getSubtotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new LocacaoResponse(
                locacao.getId(),
                locacao.getNumero(),
                locacao.getCliente() != null ? locacao.getCliente().getId() : null,
                locacao.getCliente() != null ? locacao.getCliente().getNome() : "Consumidor Final",
                locacao.getCliente() != null ? locacao.getCliente().getTelefone() : null,
                locacao.getStatus(),
                locacao.getFormaPagamento(),
                locacao.getDataRetirada(),
                locacao.getDataDevolucaoPrevista(),
                locacao.getDataDevolucaoReal(),
                locacao.getValorTotal(),
                locacao.getObservacao(),
                locacao.getItens().stream().map(ItemLocacaoResponse::from).toList(),
                locacao.getCreatedAt(),
                bruto,
                locacao.getDesconto(),
                locacao.getTipoDesconto(),
                locacao.getDescontoAplicadoEm(),
                parcelas,
                parcelaMesAtual,
                locacao.getEntrega() != null ? new EntregaResponse(
                        locacao.getEntrega().getId(),
                        locacao.getEntrega().getEndereco(),
                        locacao.getEntrega().getDataPrevista(),
                        locacao.getEntrega().getDataEntrega(),
                        locacao.getEntrega().getStatus()
                ) : null,
                locacao.getJuros(),
                locacao.getTipoJuros(),
                locacao.getDataMovimento() != null ? locacao.getDataMovimento() : null,
                locacao.getJurosAplicadoEm()
        );
    }
}