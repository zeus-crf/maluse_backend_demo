package com.example.marluse.clientes.dto;

import jakarta.validation.constraints.NotBlank;

public record ClienteAtualizarRequest(
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        String endereco
) {
}
