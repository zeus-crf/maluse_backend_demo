package com.example.marluse.vendas.model;

import com.example.marluse.estoque.model.Produto;
import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_pedido")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemPedido extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoUnitario;

    @Column(name = "custo_unitario",  precision = 10, scale = 2)
    private BigDecimal custoUnitario;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subTotal;

    @Builder.Default
    @Column(name = "baixar_estoque", nullable = false)
    private boolean baixar_estoque = true;

    @Builder.Default
    @Column(name = "permitir_sem_estoque", nullable = false)
    private boolean permitirSemEstoque = false;

    @Builder.Default
    @Column(name = "estoque_descontado", nullable = false)
    private boolean estoqueDescontado = false;
}
