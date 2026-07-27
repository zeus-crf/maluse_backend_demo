package com.example.marluse.financeiro.model;

import com.example.marluse.shared.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Uma fatia de um {@link Abatimento}: quanto daquele débito foi aplicado nesta parcela
 * ({@link LancamentoFinanceiro}). É o registro que torna o estorno possível — ele diz
 * exatamente quanto devolver para cada parcela.
 */
@Entity
@Table(name = "abatimento_parcela")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbatimentoParcela extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abatimento_id", nullable = false)
    private Abatimento abatimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lancamento_id", nullable = false)
    private LancamentoFinanceiro lancamento;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;
}
