package com.example.marluse.vendas.repository;

import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PedidoRepository extends JpaRepository<Pedido, String> {
    List<Pedido> findByStatus(StatusPedido status);

    List<Pedido> findByClienteId(String clienteId);

    @Query("SELECT COALESCE(SUM(p.valorTotal), 0) FROM Pedido p " +
            "WHERE p.status IN ('CONFIRMADO', 'PAGO') " +
            "AND p.dataMovimento BETWEEN :inicio AND :fim")
    BigDecimal somarVendasPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status = :status AND p.dataMovimento BETWEEN :inicio AND :fim")
    long contarVendasPorPeriodo(@Param("status") StatusPedido status, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT p.dataMovimento, COALESCE(SUM(p.valorTotal), 0) FROM Pedido p WHERE p.status = :status AND p.dataMovimento BETWEEN :inicio AND :fim GROUP BY p.dataMovimento")
    List<Object[]> somarVendasAgrupadoPorDia(@Param("status") StatusPedido status, @Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim);

    @Query("SELECT p.cliente.id, COALESCE(SUM(p.valorTotal), 0) FROM Pedido p WHERE p.cliente IS NOT NULL AND p.status = 'PAGO' GROUP BY p.cliente.id")
    List<Object[]> somarPorTodosClientes();

}
