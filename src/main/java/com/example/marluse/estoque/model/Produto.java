package com.example.marluse.estoque.model;

import com.example.marluse.estoque.dto.CategoriaProduto;
import com.example.marluse.estoque.enums.UnidadeMedida;
import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "produtos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Produto extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    private String descricao;

    @Column(nullable = false)
    private BigDecimal valorCompra;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "preco_diaria", precision = 10, scale = 2)
    private BigDecimal precoDiaria;

    @Builder.Default
    @Column(name = "quantidade_estoque", nullable = false, precision = 12, scale = 3)
    private BigDecimal quantidadeEstoque = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "estoque_minimo")
    private Integer estoqueMinimo = 0;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    UnidadeMedida medida;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 50)
    private CategoriaProduto categoria;

    @Builder.Default
    @Column(nullable = false)
    private boolean rascunho = false;


    @Builder.Default
    @OneToMany(mappedBy = "produto", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProdutoFornecedor> fornecedores = new ArrayList<>();

    /** Remove todos os vínculos (orphanRemoval apaga do banco). */
    public void limparFornecedores() {
        this.fornecedores.clear();
    }

    /** Adiciona um vínculo mantendo os dois lados da relação em sincronia. */
    public void addFornecedor(ProdutoFornecedor pf) {
        pf.setProduto(this);
        this.fornecedores.add(pf);
    }
}
