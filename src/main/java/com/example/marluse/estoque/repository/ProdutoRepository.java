package com.example.marluse.estoque.repository;

import com.example.marluse.estoque.model.Produto;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, String> {

    Optional<Produto> findByNome(String nome);

    List<Produto> findByAtivoTrue();

    List<Produto> findByQuantidadeEstoqueLessThanEqualAndAtivoTrue(BigDecimal quantidade);

    @EntityGraph(attributePaths = "fornecedores")
    @Query("SELECT p FROM Produto p WHERE p.quantidadeEstoque <= p.estoqueMinimo AND p.ativo = true AND p.rascunho = false")
    List<Produto> findEstoqueBaixo();


    @EntityGraph(attributePaths = "fornecedores")
    List<Produto> findByAtivoTrueAndRascunhoTrue();

    @EntityGraph(attributePaths = "fornecedores")
    List<Produto> findByAtivoTrueAndRascunhoFalse();

    @Override
    @EntityGraph(attributePaths = "fornecedores")
    Optional<Produto> findById(String id);
}
