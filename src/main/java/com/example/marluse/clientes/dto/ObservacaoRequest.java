package com.example.marluse.clientes.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ObservacaoRequest(
        @NotBlank(message = "O texto da observação é obrigatório")
        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres")
        String texto
) {}
