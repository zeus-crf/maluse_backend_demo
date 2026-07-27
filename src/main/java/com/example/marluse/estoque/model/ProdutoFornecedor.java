package com.example.marluse.estoque.model;

import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "produto_fornecedores",
        uniqueConstraints = @UniqueConstraint(name = "uk_produto_fornecedor",
                columnNames = {"produto_id", "fornecedor_id"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProdutoFornecedor extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(name = "preco_compra", precision = 10, scale = 2)
    private BigDecimal precoCompra;
}
