package com.example.marluse.estoque.service;

import com.example.marluse.estoque.dto.FornecedorResponse;
import com.example.marluse.estoque.model.Fornecedor;
import com.example.marluse.estoque.repository.FornecedorRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FornecedorService {

    private final FornecedorRepository fornecedorRepository;

    private static final int NOME_MAX = 120;

    public List<FornecedorResponse> listar(){
        return fornecedorRepository.findByAtivoTrueOrderByNomeAsc()
                .stream()
                .map(FornecedorResponse::from)
                .toList();
    }

    @Transactional
    public Set<Fornecedor> resolverPorNomes(List<String> nomes) {
        if (nomes == null) {
            return new LinkedHashSet<>();
        }

        Map<String, String> unicos = new LinkedHashMap<>();
        for (String bruto : nomes) {
            if (bruto == null) continue;
            String nome = bruto.trim();
            if (nome.isEmpty()) continue;
            if (nome.length() > NOME_MAX) nome = nome.substring(0, NOME_MAX);
            unicos.putIfAbsent(nome.toLowerCase(), nome);
        }


        Set<Fornecedor> resolvidos = new LinkedHashSet<>();
        for (String nome : unicos.values()) {
            resolvidos.add(buscarOuCriar(nome));
        }

        return resolvidos;
    }

    private Fornecedor buscarOuCriar(String nome) {
        return fornecedorRepository.findByNomeIgnoreCase(nome)
                .orElseGet(() -> fornecedorRepository.save(
                        Fornecedor.builder()
                                .nome(nome)
                                .ativo(true)
                                .build()
                ));
    }

}
