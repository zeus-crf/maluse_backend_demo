package com.example.marluse.locacoes.repository;

import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.locacoes.model.Locacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LocacaoRepository extends JpaRepository<Locacao, String> {

    List<Locacao> findByStatus(StatusLocacao status);

    List<Locacao> findByStatusIn(List<StatusLocacao> statusList);

    List<Locacao> findByClienteId(String clienteId);

    List<Locacao> findByStatusAndDataDevolucaoPrevistaBefore(StatusLocacao status, LocalDate data);

    @Query("SELECT COALESCE(SUM(l.valorTotal), 0) FROM Locacao l WHERE l.status IN (:statusList) AND l.dataMovimento BETWEEN :inicio AND :fim")
    BigDecimal somarLocacoesPorPeriodo(@Param("statusList") List<StatusLocacao> statusList, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(l) FROM Locacao l WHERE l.status IN (:statusList)")
    long contarLocacoesAtivas(@Param("statusList") List<StatusLocacao> statusList);

    @Query("SELECT l.dataMovimento, COALESCE(SUM(l.valorTotal), 0) FROM Locacao l WHERE l.dataMovimento BETWEEN :inicio AND :fim GROUP BY l.dataMovimento")
    List<Object[]> somarLocacoesAgrupadoPorDia(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT l.cliente.id, COALESCE(SUM(l.valorTotal), 0) FROM Locacao l WHERE l.cliente IS NOT NULL AND l.status = 'DEVOLVIDA' GROUP BY l.cliente.id")
    List<Object[]> somarPorTodosClientes();

    @Modifying
    @Query("UPDATE Locacao l SET l.status = :novo WHERE l.status = :atual AND l.dataDevolucaoPrevista < :hoje")
    int marcarAtrasadas(@Param("atual") StatusLocacao atual,
                        @Param("novo")  StatusLocacao novo,
                        @Param("hoje")  LocalDate hoje);
}