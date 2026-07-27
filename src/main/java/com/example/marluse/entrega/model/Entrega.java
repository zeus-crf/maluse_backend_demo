package com.example.marluse.entrega.model;

import com.example.marluse.entrega.enums.StatusEntrega;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.shared.BaseEntity;
import com.example.marluse.vendas.model.Pedido;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "entregas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Entrega  extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "pedido_id")
    private Pedido pedido;

    @OneToOne
    @JoinColumn(name = "locacao_id")
    private Locacao locacao;

    private String endereco;

    private LocalDate dataPrevista;

    private LocalDate dataEntrega;

    @Enumerated(EnumType.STRING)
    private StatusEntrega status;


}
