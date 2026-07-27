package com.example.marluse.vendas.model;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.entrega.model.Entrega;
import com.example.marluse.shared.BaseEntity;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pedido extends BaseEntity {

    @Column(nullable = false)
    private Long numero;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @Column(name = "forma_pagamento")
    private FormaPagamento formaPagamento;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    private String observacao;

    @Column(name = "data_vencimento")
    private LocalDate dataVencimento;

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
    @OneToMany(mappedBy = "pedido", cascade =  CascadeType.ALL, orphanRemoval = true)
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToOne(mappedBy = "pedido", cascade = CascadeType.ALL, optional = true)
    private Entrega entrega;

    @Column(name = "data_movimento")
    private LocalDate dataMovimento;

    /** true quando o estoque já foi baixado (imediatamente se sem entrega; ao confirmar entrega se tiver entrega) */
    @Builder.Default
    @Column(name = "estoque_descontado", nullable = false)
    private boolean estoqueDescontado = false;
}
