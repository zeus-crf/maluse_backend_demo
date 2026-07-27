package com.example.marluse.dashboard.service;

import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.dashboard.dto.DashboardKpisResponse;
import com.example.marluse.dashboard.dto.EstoqueCriticoResponse;
import com.example.marluse.dashboard.dto.GraficoItemResponse;
import com.example.marluse.dashboard.dto.LocacaoEmCursoResponse;
import com.example.marluse.estoque.repository.ProdutoRepository;
import com.example.marluse.financeiro.repository.AbatimentoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.locacoes.repository.LocacaoRepository;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.repository.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PedidoRepository pedidoRepository;
    private final LocacaoRepository locacaoRepository;
    private final ClienteRepository clienteRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final AbatimentoRepository abatimentoRepository;
    private final ProdutoRepository produtoRepository;

    public DashboardKpisResponse getKpis(LocalDate inicio, LocalDate fim){

        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atStartOfDay();

        // Receita = parcelas quitadas à vista + abatimentos de dívida recebidos no período
        BigDecimal receitaPeriodo = lancamentoRepository.somarReceitaPorPeriodo(inicio, fim)
                .add(abatimentoRepository.somarAbatimentosPorPeriodo(inicio, fim));
        long clientesAtivos = clienteRepository.countByAtivoTrue();


        List<StatusLocacao> statusAtivos = List.of(StatusLocacao.ATIVA, StatusLocacao.ATRASADA);
        long locacoesAtivas = locacaoRepository.contarLocacoesAtivas(statusAtivos);

        BigDecimal vendasValor = pedidoRepository.somarVendasPorPeriodo(inicio, fim);
        long vendasQuantidade = pedidoRepository.contarVendasPorPeriodo(StatusPedido.CONFIRMADO, inicio, fim);
        BigDecimal locacoesAtivasValor = locacaoRepository.somarLocacoesPorPeriodo(statusAtivos, inicio, fim);



        return new DashboardKpisResponse(
                receitaPeriodo,
                vendasValor,
                vendasQuantidade,
                locacoesAtivas,
                locacoesAtivasValor,
                clientesAtivos
        );
    }

    public List<GraficoItemResponse> getGrafico(LocalDate inicio, LocalDate fim) {
        LocalDateTime inicioDt = inicio.atStartOfDay();
        LocalDateTime fimDt = fim.atTime(23, 59, 59);

        Map<LocalDate, BigDecimal> vendas = pedidoRepository
                .somarVendasAgrupadoPorDia(StatusPedido.CONFIRMADO, inicio, fim)
                .stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (BigDecimal) row[1]
                ));

        Map<LocalDate, BigDecimal> locacoes = locacaoRepository
                .somarLocacoesAgrupadoPorDia(inicio, fim)
                .stream()
                .collect(Collectors.toMap(
                        row -> (LocalDate) row[0],
                        row -> (BigDecimal) row[1]
                ));

        List<GraficoItemResponse> resultado = new ArrayList<>();
        LocalDate dia = inicio;
        while (!dia.isAfter(fim)) {
            resultado.add(new GraficoItemResponse(
                    dia,
                    vendas.getOrDefault(dia, BigDecimal.ZERO),
                    locacoes.getOrDefault(dia, BigDecimal.ZERO)
            ));
            dia = dia.plusDays(1);
        }
        return resultado;
    }

    public List<EstoqueCriticoResponse> getEstoqueCritico() {
        // Antes: findAll() trazia o catálogo inteiro para filtrar em Java.
        // Agora usa a query findEstoqueBaixo() (WHERE quantidade <= minimo AND ativo)
        // que já existe no repositório — só os produtos críticos vêm do banco.
        return produtoRepository.findEstoqueBaixo()
                .stream()
                .map(p -> new EstoqueCriticoResponse(
                        p.getId(),
                        p.getNome(),
                        p.getQuantidadeEstoque(),
                        p.getEstoqueMinimo(),
                        p.getPreco()
                ))
                .toList();
    }

    public List<LocacaoEmCursoResponse> getLocacoesEmCurso() {
        return locacaoRepository.findByStatusIn(List.of(StatusLocacao.ATIVA, StatusLocacao.ATRASADA))
                .stream()
                .map(l -> new LocacaoEmCursoResponse(
                        l.getId(),
                        l.getCliente() != null ? l.getCliente().getNome() : "Consumidor Final",
                        l.getItens().isEmpty() ? "-" : l.getItens().get(0).getProduto().getNome(),
                        l.getDataDevolucaoPrevista(),
                        l.getStatus()
                ))
                .toList();
    }



}
