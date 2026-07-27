package com.example.marluse.locacoes.model;

import com.example.marluse.estoque.model.Produto;
import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "itens_locacao")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemLocacao extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locacao_id", nullable = false)
    private Locacao locacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidade;

    @Column(name = "preco_diaria", nullable = false, precision = 10, scale = 2)
    private BigDecimal precoDiaria;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

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
