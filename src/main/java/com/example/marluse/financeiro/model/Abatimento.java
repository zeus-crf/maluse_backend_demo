package com.example.marluse.financeiro.model;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Um débito registrado contra a dívida do cliente — o valor total que o operador digitou.
 * A distribuição desse valor pelas parcelas em aberto fica em {@link AbatimentoParcela},
 * valendo a invariante: SUM(parcelas.valor) == this.valor.
 */
@Entity
@Table(name = "abatimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Abatimento extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate data;

    private String observacao;

    @Builder.Default
    @Column(nullable = false)
    private boolean estornado = false;

    @Column(name = "estornado_em")
    private LocalDate estornadoEm;

    @Builder.Default
    @OneToMany(mappedBy = "abatimento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AbatimentoParcela> parcelas = new ArrayList<>();
}
