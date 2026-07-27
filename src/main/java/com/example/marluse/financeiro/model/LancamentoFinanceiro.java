package com.example.marluse.financeiro.model;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.financeiro.enums.Recorrencia;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.shared.BaseEntity;
import com.example.marluse.vendas.model.Pedido;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "lancamento_financeiros")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LancamentoFinanceiro extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoLancamento tipo;

    @Column(nullable = false)
    private String categoria;

    @Column(nullable = false)
    private String descricao;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Builder.Default
    @Column(name = "valor_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorPago = BigDecimal.ZERO;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    private LocalDate dataPagamento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private StatusLancamento status = StatusLancamento.PENDENTE;

    @Enumerated(EnumType.STRING)
    private Recorrencia recorrencia;

    @Column(name = "recorrencia_grupo_id")
    private String recorrenciaGrupoId;

    @Column(name = "recorrencia_ativa")
    @Builder.Default
    private Boolean recorrenciaAtiva = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "locacao_id")
    private Locacao locacao;

    @Column(name = "num_parcelas")
    private Integer numParcelas;

    @Column(name = "total_parcelas")
    private Integer totalParcelas;


}
