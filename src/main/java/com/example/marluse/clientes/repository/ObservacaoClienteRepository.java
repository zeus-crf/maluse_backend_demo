package com.example.marluse.clientes.repository;

import com.example.marluse.clientes.model.ObservacaoCliente;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObservacaoClienteRepository extends JpaRepository<ObservacaoCliente, String> {

    /** Anotações de um cliente, da mais recente para a mais antiga. */
    List<ObservacaoCliente> findByClienteIdOrderByCreatedAtDesc(String clienteId);
}
