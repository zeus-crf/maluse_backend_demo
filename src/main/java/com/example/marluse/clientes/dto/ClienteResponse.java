package com.example.marluse.clientes.dto;

import com.example.marluse.clientes.model.Cliente;

import java.math.BigDecimal;

public record ClienteResponse(
        String id,
        String nome,
        String cpfCnpj,
        String telefone,
        String email,
        String endereco,
        boolean consumidorFinal,
        boolean ativo,
        BigDecimal totalGasto
) {
    public static ClienteResponse from(Cliente cliente, BigDecimal totalGasto) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNome(),
                cliente.getCpfCnpj(),
                cliente.getTelefone(),
                cliente.getEmail(),
                cliente.getEndereco(),
                cliente.isConsumidorFinal(),
                cliente.isAtivo(),
                totalGasto
        );
    }

    public static ClienteResponse from(Cliente cliente) {
        return from(cliente, BigDecimal.ZERO);
    }
}
