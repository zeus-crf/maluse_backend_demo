package com.example.marluse.clientes.dto;

import com.example.marluse.clientes.model.ObservacaoCliente;

import java.time.LocalDateTime;

public record ObservacaoResponse(
        String id,
        String texto,
        String autorNome,
        LocalDateTime criadaEm
) {
    public static ObservacaoResponse from(ObservacaoCliente o) {
        return new ObservacaoResponse(
                o.getId(),
                o.getTexto(),
                o.getAutor() != null ? o.getAutor().getNome() : null,
                o.getCreatedAt()
        );
    }
}
