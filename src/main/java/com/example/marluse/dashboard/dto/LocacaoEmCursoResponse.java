package com.example.marluse.dashboard.dto;

import com.example.marluse.locacoes.enums.StatusLocacao;

import java.time.LocalDate;

public record LocacaoEmCursoResponse(
        String id,
        String clienteNome,
        String produtoNome,
        LocalDate dataDevolucao,
        StatusLocacao status
) {
}
