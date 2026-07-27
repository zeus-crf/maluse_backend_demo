package com.example.marluse.clientes.model;

import com.example.marluse.security.model.Usuario;
import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Anotação livre sobre um cliente, registrada por um usuário.
 *
 * <p>A data da anotação é o {@code createdAt} herdado do {@link BaseEntity} — não há campo de data
 * próprio nem digitação manual. Observações são imutáveis: para corrigir, apaga-se e escreve-se
 * outra.
 */
@Entity
@Table(name = "observacoes_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObservacaoCliente extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, length = 1000)
    private String texto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autor_id", nullable = false)
    private Usuario autor;
}
