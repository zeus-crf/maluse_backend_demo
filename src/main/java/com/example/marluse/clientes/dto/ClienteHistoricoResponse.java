package com.example.marluse.clientes.dto;

import java.util.List;

public record ClienteHistoricoResponse(
        List<PedidoResumo> pedidos,
        List<LocacaoResumo> locacoes
) {
}
