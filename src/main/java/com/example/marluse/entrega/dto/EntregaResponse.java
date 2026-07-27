package com.example.marluse.entrega.dto;

import com.example.marluse.entrega.enums.StatusEntrega;

import java.time.LocalDate;

public record EntregaResponse(
        String id,
        String endereco,
        LocalDate dataPrevista,
        LocalDate dataEntrega,
        StatusEntrega status
) {
}
