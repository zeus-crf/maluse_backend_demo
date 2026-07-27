package com.example.marluse.estoque.model;

import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "fornecedores")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Fornecedor extends BaseEntity {

    @Column(nullable = false, unique = true, length = 120)
    private String nome;

    @Builder.Default
    @Column(nullable = false)
    private boolean ativo = true;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Fornecedor)) return false;
        return getId() != null && getId().equals(((Fornecedor) o).getId());
    }

    @Override
    public int hashCode() {
        return Fornecedor.class.hashCode();
    }
}
