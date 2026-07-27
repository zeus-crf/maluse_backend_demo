package com.example.marluse.locacoes.model;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.entrega.model.Entrega;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.shared.BaseEntity;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "locacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Locacao extends BaseEntity {

    @Column(nullable = false)
    private Long numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Column(nullable = false, columnDefinition = "VARCHAR(20)")
    @Enumerated(EnumType.STRING)
    private StatusLocacao status;

    @Column(name = "forma_pagamento")
    @Enumerated(EnumType.STRING)
    private FormaPagamento formaPagamento;

    @Column(name = "data_retirada", nullable = false)
    private LocalDate dataRetirada;

    @Column(name = "data_devolucao_prevista", nullable = false)
    private LocalDate dataDevolucaoPrevista;

    @Column(name = "data_devolucao_real")
    private LocalDate dataDevolucaoReal;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    private String observacao;

    @Column(name = "desconto", precision = 10, scale = 2)
    private BigDecimal desconto;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_desconto", length = 10)
    private TipoDesconto tipoDesconto;

    @Column(name = "desconto_aplicado_em")
    private LocalDate descontoAplicadoEm;

    @Column(name = "juros", precision = 10, scale = 2)
    private BigDecimal juros;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_juros", length = 10)
    private TipoDesconto tipoJuros;

    @Column(name = "juros_aplicado_em")
    private LocalDate jurosAplicadoEm;

    @Builder.Default
    @OneToMany(mappedBy = "locacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ItemLocacao> itens = new ArrayList<>();

    @OneToOne(mappedBy = "locacao", cascade = CascadeType.ALL, optional = true)
    private Entrega entrega;

    @Column(name = "data_movimento")
    private LocalDate dataMovimento;

    /** true quando o estoque já foi baixado (imediatamente se sem entrega; ao confirmar entrega se tiver entrega) */
    @Builder.Default
    @Column(name = "estoque_descontado", nullable = false)
    private boolean estoqueDescontado = false;

}
