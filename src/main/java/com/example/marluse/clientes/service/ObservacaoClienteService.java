package com.example.marluse.clientes.service;

import com.example.marluse.clientes.dto.ObservacaoRequest;
import com.example.marluse.clientes.dto.ObservacaoResponse;
import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.model.ObservacaoCliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.clientes.repository.ObservacaoClienteRepository;
import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Anotações livres sobre um cliente. Observações são imutáveis: criar, listar e apagar —
 * para corrigir, apaga-se e escreve-se outra.
 */
@Service
@RequiredArgsConstructor
public class ObservacaoClienteService {

    private final ObservacaoClienteRepository observacaoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public ObservacaoResponse criar(String clienteId, ObservacaoRequest request, String emailAutor) {
        Cliente cliente = clienteRepository.findById(clienteId)
                .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));

        Usuario autor = usuarioRepository.findByEmail(emailAutor)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        ObservacaoCliente observacao = ObservacaoCliente.builder()
                .cliente(cliente)
                .texto(request.texto().trim())
                .autor(autor)
                .build();

        return ObservacaoResponse.from(observacaoRepository.save(observacao));
    }

    public List<ObservacaoResponse> listar(String clienteId) {
        if (!clienteRepository.existsById(clienteId)) {
            throw new EntityNotFoundException("Cliente não encontrado");
        }

        return observacaoRepository.findByClienteIdOrderByCreatedAtDesc(clienteId)
                .stream()
                .map(ObservacaoResponse::from)
                .toList();
    }

    @Transactional
    public void deletar(String observacaoId) {
        ObservacaoCliente observacao = observacaoRepository.findById(observacaoId)
                .orElseThrow(() -> new EntityNotFoundException("Observação não encontrada"));

        observacaoRepository.delete(observacao);
    }
}
