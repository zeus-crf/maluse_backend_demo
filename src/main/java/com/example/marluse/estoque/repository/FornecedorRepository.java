package com.example.marluse.estoque.repository;

import com.example.marluse.estoque.model.Fornecedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Optional;

@Repository
public interface FornecedorRepository extends JpaRepository<Fornecedor, String> {

    Optional<Fornecedor> findByNomeIgnoreCase(String nome);

    List<Fornecedor> findByAtivoTrueOrderByNomeAsc();
}
