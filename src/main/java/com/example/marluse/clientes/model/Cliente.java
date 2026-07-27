package com.example.marluse.clientes.model;

import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente extends BaseEntity {

    @Column(nullable = false)
    private String nome;

    @Column(name = "cpf_cnpj", unique = true)
    private String cpfCnpj;

    private String telefone;

    private String email;

    private String endereco;

    @Column(name = "consumidor_final", nullable = false)
    private boolean consumidorFinal = false;

    @Column(nullable = false)
    private boolean ativo = true;
}