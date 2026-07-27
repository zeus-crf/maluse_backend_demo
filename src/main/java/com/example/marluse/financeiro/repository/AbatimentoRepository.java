package com.example.marluse.financeiro.repository;

import com.example.marluse.financeiro.model.Abatimento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface AbatimentoRepository extends JpaRepository<Abatimento, String> {

    List<Abatimento> findByClienteIdOrderByDataDesc(String clienteId);

    /** Soma dos abatimentos (não estornados) recebidos no período — dinheiro que entrou no caixa. */
    @Query("SELECT COALESCE(SUM(a.valor), 0) FROM Abatimento a " +
            "WHERE a.estornado = false AND a.data BETWEEN :inicio AND :fim")
    BigDecimal somarAbatimentosPorPeriodo(@Param("inicio") LocalDate inicio,
                                          @Param("fim") LocalDate fim);
}
