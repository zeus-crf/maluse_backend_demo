package com.example.marluse.entrega.dto;

import java.time.LocalDate;

public record EntregaRequest(
        String endereco,
        LocalDate dataPrevista
) {
}
