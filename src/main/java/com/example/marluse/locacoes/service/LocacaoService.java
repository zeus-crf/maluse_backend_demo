package com.example.marluse.locacoes.service;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.entrega.enums.StatusEntrega;
import com.example.marluse.entrega.model.Entrega;
import com.example.marluse.estoque.dto.ProdutoResponse;
import com.example.marluse.estoque.model.Produto;
import com.example.marluse.estoque.repository.ProdutoRepository;
import com.example.marluse.estoque.service.ProdutoService;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.financeiro.service.LancamentoFinanceiroService;
import com.example.marluse.locacoes.dto.ItemLocacaoRequest;
import com.example.marluse.locacoes.dto.ItemLocacaoResponse;
import com.example.marluse.locacoes.dto.LocacaoEdicaoRequest;
import com.example.marluse.locacoes.dto.LocacaoRequest;
import com.example.marluse.locacoes.dto.LocacaoResponse;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.locacoes.model.ItemLocacao;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.locacoes.repository.LocacaoRepository;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.TipoDesconto;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.example.marluse.financeiro.dto.ParcelaResponse;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LocacaoService {

    private final LocacaoRepository locacaoRepository;
    private final ProdutoRepository produtoRepository;
    private final ClienteRepository clienteRepository;
    private final LancamentoFinanceiroService lancamentoService;
    private final LancamentoFinanceiroRepository lancamentoRepository;
    private final ProdutoService produtoService;

    @Transactional
    public LocacaoResponse criar(LocacaoRequest request, boolean isOrcamento){
        Cliente cliente = null;
        if (request.clienteId() != null) {
            cliente = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        }

        if (!request.dataDevolucaoPrevista().isAfter(request.dataRetirada())){
            throw new IllegalArgumentException("A data de devolução deve ser posterior à data de retirada");
        }

        // Fiado e parcelado exigem cliente cadastrado — não faz sentido registrar dívida sem devedor
        int numParcelas = request.numeroParcelas() != null && request.numeroParcelas() > 1
                ? request.numeroParcelas() : 1;
        boolean isPendente = request.formaPagamento() == FormaPagamento.FIADO || numParcelas > 1;
        if (isPendente && cliente == null) {
            throw new IllegalArgumentException(
                    "Pagamento fiado ou parcelado exige um cliente cadastrado");
        }

        // Status inicial: query param tem prioridade; fallback para campo do body ou ATIVA
        StatusLocacao statusInicial = isOrcamento ? StatusLocacao.ORCAMENTO
                : (request.status() != null ? request.status() : StatusLocacao.ATIVA);

        // Calcula os dias entre data de retirada e data de devolução prevista
        long dias = ChronoUnit.DAYS.between(request.dataRetirada(), request.dataDevolucaoPrevista());

        Locacao locacao = Locacao.builder()
                .status(statusInicial)
                .formaPagamento(request.formaPagamento())
                .dataRetirada(request.dataRetirada())
                .dataDevolucaoPrevista(request.dataDevolucaoPrevista())
                .observacao(request.observacao())
                .valorTotal(BigDecimal.ZERO)
                .dataMovimento(request.dataMovimento() != null ? request.dataMovimento() : LocalDate.now() )
                .build();

        locacao.setCliente(cliente);

        BigDecimal total = BigDecimal.ZERO;

        // Só desconta estoque na criação se não houver entrega — sem entrega significa retirada no local
        boolean temEntrega = request.entrega() != null;

        for (ItemLocacaoRequest itemRequest : request.itens()){
            Produto produto;
            if (itemRequest.isProdutoNovo()){
                produto = produtoService.criarRascunho(itemRequest.produtoNome(), itemRequest.precoDiaria(), itemRequest.precoDiaria());
            } else if (itemRequest.produtoId() != null && !itemRequest.produtoId().isBlank()) {
                produto = produtoRepository.findById(itemRequest.produtoId())
                        .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
            } else {
                throw new IllegalArgumentException("Item inválido: informe um produto existente ou o nome de um produto novo");
            }

            boolean baixar =  itemRequest.baixarEstoque();
            boolean permitir = Boolean.TRUE.equals(itemRequest.permitirSemEstoque());

            BigDecimal diaria = itemRequest.precoDiaria() != null
                    ? itemRequest.precoDiaria()
                    : (produto.getPrecoDiaria() !=  null ?
                    produto.getPrecoDiaria() : produto.getPreco());


            BigDecimal subtotal = diaria
                    .multiply(itemRequest.quantidade())
                    .multiply(BigDecimal.valueOf(dias));

            ItemLocacao item = ItemLocacao.builder()
                    .locacao(locacao)
                    .produto(produto)
                    .quantidade(itemRequest.quantidade())
                    .precoDiaria(produto.getPreco())
                    .precoDiaria(diaria)
                    .subtotal(subtotal)
                    .baixar_estoque(baixar)
                    .permitirSemEstoque(permitir)
                    .build();

            // Orçamento nunca baixa; com entrega, baixa só na confirmação da entrega
            if (statusInicial != StatusLocacao.ORCAMENTO && !temEntrega && baixar) {
                baixarEstoque(item);
            }

            locacao.getItens().add(item);
            total = total.add(subtotal);
        }

        locacao.setEstoqueDescontado(
                locacao.getItens().stream().anyMatch(ItemLocacao::isEstoqueDescontado));


        locacao.setDesconto(request.desconto());
        locacao.setTipoDesconto(request.tipoDesconto());
        locacao.setJuros(request.juros());
        locacao.setTipoJuros(request.tipoJuros());
        if (request.desconto() != null) locacao.setDescontoAplicadoEm(LocalDate.now());
        if (request.juros() != null) locacao.setJurosAplicadoEm(LocalDate.now());
        if (request.entrega() != null ) locacao.setEntrega(new Entrega( null, locacao, request.entrega().endereco(), request.entrega().dataPrevista(), null, StatusEntrega.PENDENTE));
        BigDecimal valorFinal = aplicarDesconto(total, request.desconto(), request.tipoDesconto());
        valorFinal = aplicarJuros(valorFinal, request.juros(), request.tipoJuros());

        locacao.setValorTotal(valorFinal);

        // Gera número sequencial antes de salvar
        long numeroLocacao = locacaoRepository.count() + 1;
        locacao.setNumero(numeroLocacao);

        Locacao locacaoSalva = locacaoRepository.save(locacao);

        // Integração financeira — apenas para locações confirmadas (não orçamento)
        if (statusInicial != StatusLocacao.ORCAMENTO) {
            String nomeCliente = cliente != null ? cliente.getNome() : "Consumidor Final";

            int parcelas = request.numeroParcelas() != null && request.numeroParcelas() > 1
                    ? request.numeroParcelas() : 1;
            // Locações sempre iniciam PENDENTE — o pagamento é confirmado na devolução
            String descricao = request.formaPagamento() == FormaPagamento.FIADO
                    ? String.format("Locação fiado #%03d - %s", numeroLocacao, nomeCliente)
                    : String.format("Locação #%03d - %s", numeroLocacao, nomeCliente);
            LocalDate vencimento = request.primeiroVencimento() != null ? request.primeiroVencimento()
                    : locacaoSalva.getDataDevolucaoPrevista().plusDays(1);

            lancamentoService.registarLocacaoReceita(locacaoSalva, descricao, valorFinal, StatusLancamento.PENDENTE, vencimento, parcelas);
        }

        return toResponse(locacaoSalva);
    }

    @Transactional
    public List<LocacaoResponse> listarTodas(){
        // Próxima parcela PENDENTE por locação (parceladas com pagamento em aberto)
        Map<String, ParcelaResponse> proximaPendente = new HashMap<>();
        lancamentoRepository.findProximasParcelasPendentesLocacoes().forEach(l -> {
            if (l.getLocacao() != null) {
                proximaPendente.putIfAbsent(l.getLocacao().getId(), ParcelaResponse.from(l));
            }
        });

        // Última parcela PAGA para locações totalmente pagas (exibe "N/N pagas")
        Map<String, ParcelaResponse> ultimaPaga = new HashMap<>();
        lancamentoRepository.findUltimasParcelasDeLocacoesPagas().forEach(l -> {
            if (l.getLocacao() != null) {
                ultimaPaga.putIfAbsent(l.getLocacao().getId(), ParcelaResponse.from(l));
            }
        });

        // PENDENTE tem prioridade sobre PAGO
        Map<String, ParcelaResponse> parcelaPorLocacao = new HashMap<>(ultimaPaga);
        parcelaPorLocacao.putAll(proximaPendente);

        return locacaoRepository.findAll()
                .stream()
                .map(l -> LocacaoResponse.from(l, null, parcelaPorLocacao.get(l.getId())))
                .toList();
    }

    public LocacaoResponse listarPorId(String id) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));
        List<ParcelaResponse> parcelas = listarParcelas(id);
        return LocacaoResponse.from(locacao, parcelas);
    }

    public List<LocacaoResponse> listarPorStatus(StatusLocacao status) {
        return locacaoRepository.findByStatus(status)
                .stream()
                .map(LocacaoResponse::from)
                .toList();
    }

    public List<ParcelaResponse> listarParcelas(String locacaoId) {
        return lancamentoRepository.findByLocacaoId(locacaoId).stream()
                .sorted(Comparator.comparing(LancamentoFinanceiro::getDataVencimento,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(ParcelaResponse::from)
                .toList();
    }

    public List<LocacaoResponse> listarAtrasadas() {
        return locacaoRepository.findByStatusAndDataDevolucaoPrevistaBefore(StatusLocacao.ATIVA, LocalDate.now())
                .stream()
                .map(LocacaoResponse::from)
                .toList();
    }

    @Transactional
    public LocacaoResponse editar(String id, LocacaoEdicaoRequest request) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        if (locacao.getStatus() == StatusLocacao.CANCELADA || locacao.getStatus() == StatusLocacao.DEVOLVIDA) {
            throw new IllegalArgumentException("Não é possível editar uma locação " + locacao.getStatus().name().toLowerCase());
        }

        if (request.dataMovimento() != null) locacao.setDataMovimento(request.dataMovimento());

        if (request.desconto() != null) {
            locacao.setDesconto(request.desconto());
            locacao.setTipoDesconto(request.tipoDesconto());
            locacao.setDescontoAplicadoEm(LocalDate.now());
            // Aplica desconto sobre o valorTotal atual (cumulativo)
            BigDecimal novoTotal = aplicarDesconto(locacao.getValorTotal(), request.desconto(), request.tipoDesconto());
            locacao.setValorTotal(novoTotal);


            // Redistribui valor entre parcelas ainda não pagas
            List<LancamentoFinanceiro> pendentes =
                    lancamentoRepository.findByLocacaoIdAndStatusNot(id, StatusLancamento.PAGO);
            if (!pendentes.isEmpty()) {
                BigDecimal pago = lancamentoRepository.findByLocacaoId(id).stream()
                        .filter(l -> l.getStatus() == StatusLancamento.PAGO)
                        .map(LancamentoFinanceiro::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal restante = novoTotal.subtract(pago);
                if (restante.compareTo(BigDecimal.ZERO) < 0)
                    throw new IllegalArgumentException("Desconto resulta em valor menor que o já pago");
                BigDecimal valorParcela = restante.divide(
                        BigDecimal.valueOf(pendentes.size()), 2, RoundingMode.HALF_UP);
                pendentes.forEach(l -> {
                    l.setValor(valorParcela);
                    lancamentoRepository.save(l);
                });
            }
        }

        if (request.juros() != null) {
            locacao.setJuros(request.juros());
            locacao.setTipoJuros(request.tipoJuros());
            locacao.setJurosAplicadoEm(LocalDate.now());
            BigDecimal totalComJuros = aplicarJuros(locacao.getValorTotal(), request.juros(), request.tipoJuros());
            locacao.setValorTotal(totalComJuros);

            // Redistribui valor entre parcelas ainda não pagas
            List<LancamentoFinanceiro> pendentes =
                    lancamentoRepository.findByLocacaoIdAndStatusNot(id, StatusLancamento.PAGO);
            if (!pendentes.isEmpty()) {
                BigDecimal pago = lancamentoRepository.findByLocacaoId(id).stream()
                        .filter(l -> l.getStatus() == StatusLancamento.PAGO)
                        .map(LancamentoFinanceiro::getValor)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal restante = totalComJuros.subtract(pago);
                BigDecimal valorParcela = restante.divide(
                        BigDecimal.valueOf(pendentes.size()), 2, RoundingMode.HALF_UP);
                pendentes.forEach(l -> {
                    l.setValor(valorParcela);
                    lancamentoRepository.save(l);
                });
            }
        }

        locacao.setFormaPagamento(request.formaPagamento());
        locacao.setObservacao(request.observacao());

        return LocacaoResponse.from(locacaoRepository.save(locacao));
    }

    @Transactional
    public LocacaoResponse devolver(String id){
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada!"));

        if (locacao.getStatus() != StatusLocacao.ATIVA && locacao.getStatus() != StatusLocacao.ATRASADA){
            throw new IllegalArgumentException("Locação não pode ser devolvida no status atual");
        }

        // Só restaura estoque se ele foi de fato baixado
        for (ItemLocacao item : locacao.getItens()) {
            if (item.isEstoqueDescontado()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().add(item.getQuantidade()));
                produtoRepository.save(produto);
                item.setEstoqueDescontado(false);
            }
        }

        lancamentoRepository.findByLocacaoId(id).forEach(l -> {
            if (l.getStatus() != StatusLancamento.PAGO) {
                l.setStatus(StatusLancamento.PAGO);
                l.setDataPagamento(LocalDate.now());
                lancamentoRepository.save(l);
            }
        });

        locacao.setStatus(StatusLocacao.DEVOLVIDA);
        locacao.setDataDevolucaoReal(LocalDate.now());
        return LocacaoResponse.from(locacaoRepository.save(locacao));
    }

    @Transactional
    public void deletar(String id) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        // Restaura estoque somente se ele foi de fato baixado
        if (locacao.isEstoqueDescontado()) {
            for (ItemLocacao item : locacao.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().add(item.getQuantidade()));
                produtoRepository.save(produto);
            }
        }

        // Remove lançamentos financeiros vinculados antes de apagar a locação
        lancamentoRepository.deleteByLocacaoId(id);

        locacaoRepository.delete(locacao);
    }

    @Transactional
    public LocacaoResponse cancelar(String id){
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        if (locacao.getStatus() == StatusLocacao.CANCELADA){
            throw new IllegalArgumentException("A locação já está cancelada");
        }

        if (locacao.isEstoqueDescontado()) {
            for (ItemLocacao item : locacao.getItens()) {
                Produto produto = item.getProduto();
                produto.setQuantidadeEstoque(produto.getQuantidadeEstoque().add(item.getQuantidade()));
                produtoRepository.save(produto);
            }
        }

        lancamentoRepository.findByLocacaoId(id).forEach(l -> {
            l.setStatus(StatusLancamento.CANCELADO);
            lancamentoRepository.save(l);
        });

        locacao.setStatus(StatusLocacao.CANCELADA);
        return LocacaoResponse.from(locacaoRepository.save(locacao));
    }

    private BigDecimal aplicarDesconto(BigDecimal bruto, BigDecimal desconto, TipoDesconto tipo) {
        if (desconto == null || desconto.compareTo(BigDecimal.ZERO) == 0 ) return bruto;

        BigDecimal calc = tipo == TipoDesconto.PERCENTUAL
                ? bruto.multiply(desconto).divide(BigDecimal.valueOf(100))
                : desconto;

        BigDecimal resulto = bruto.subtract(calc);

        if (resulto.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Desconto não pode ser maior que o valor total");
        return resulto;
    }

    private BigDecimal aplicarJuros(BigDecimal base, BigDecimal juros, TipoDesconto tipo) {
        if (juros == null || juros.compareTo(BigDecimal.ZERO) == 0) return base;

        BigDecimal calc = tipo == TipoDesconto.PERCENTUAL
                ? base.multiply(juros).divide(BigDecimal.valueOf(100))
                : juros;

        return base.add(calc);
    }

    @Transactional
    public LocacaoResponse confirmar(String id) {
        Locacao locacao = locacaoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Locação não encontrada"));

        if (locacao.getStatus() != StatusLocacao.ORCAMENTO) {
            throw new IllegalArgumentException("Apenas orçamentos podem ser confirmados");
        }

        // Só desconta estoque se não houver entrega (retirada no local)
        boolean temEntregaConfirmar = locacao.getEntrega() != null;
        if (!temEntregaConfirmar) {
            for (ItemLocacao item : locacao.getItens()) {
              if (item.isBaixar_estoque() && !item.isEstoqueDescontado()){
                  baixarEstoque(item);
              }
            }
            locacao.setEstoqueDescontado(
                    locacao.getItens()
                            .stream()
                            .anyMatch(ItemLocacao::isEstoqueDescontado)
            );
        }

        // Cria lançamento PENDENTE (confirmado na devolução)
        String nomeCliente = locacao.getCliente() != null ? locacao.getCliente().getNome() : "Consumidor Final";
        String descricao = locacao.getFormaPagamento() == FormaPagamento.FIADO
                ? String.format("Locação fiado #%03d - %s", locacao.getNumero(), nomeCliente)
                : String.format("Locação #%03d - %s", locacao.getNumero(), nomeCliente);
        LocalDate vencimento = locacao.getDataDevolucaoPrevista().plusDays(1);

        lancamentoService.registarLocacaoReceita(locacao, descricao, locacao.getValorTotal(), StatusLancamento.PENDENTE, vencimento, 1);

        // ATIVA ou já ATRASADA se a data prevista passou
        StatusLocacao novoStatus = locacao.getDataDevolucaoPrevista().isBefore(LocalDate.now())
                ? StatusLocacao.ATRASADA
                : StatusLocacao.ATIVA;
        locacao.setStatus(novoStatus);

        return LocacaoResponse.from(locacaoRepository.save(locacao));
    }

    public BigDecimal somarLocacoesPorPeriodo(LocalDate inicio, LocalDate fim) {
        return lancamentoRepository.somarReceitaLocacoesPorPagamento(inicio, fim);
    }

    private void baixarEstoque(ItemLocacao item) {
        Produto produto = item.getProduto();
        BigDecimal novoSaldo = produto.getQuantidadeEstoque().subtract(item.getQuantidade());
        if (novoSaldo.signum() < 0 && !item.isPermitirSemEstoque()) {
            throw new IllegalArgumentException("Estoque insuficiente para" + produto.getNome());
        }
        produto.setQuantidadeEstoque(novoSaldo);
        produtoRepository.save(produto);
        item.setEstoqueDescontado(true);
    }

    private LocacaoResponse toResponse(Locacao locacao) {
        return LocacaoResponse.from(locacao);
    }
}
