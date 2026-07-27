package com.example.marluse.dashboard.dto;

import java.math.BigDecimal;

public record EstoqueCriticoResponse(
        String id,
        String nome,
        BigDecimal quantidadeAtual,
        int estoqueMinimo,
        BigDecimal preco
){
}
