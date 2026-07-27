package com.example.marluse.entrega.dto;

import java.time.LocalDate;

public record EntregaAtualizarRequest(
        String endereco,
        LocalDate dataPrevista
) {
}
