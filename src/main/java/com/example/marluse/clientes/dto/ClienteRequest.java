package com.example.marluse.clientes.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteRequest(
        @NotBlank(message = "Nome é obrigatório")
        String nome,

        String cpfCnpj,
        String telefone,
        String email,
        String endereco,
        boolean consumidorFinal
) {
}
