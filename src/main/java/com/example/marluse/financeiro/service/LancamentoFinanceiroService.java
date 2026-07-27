package com.example.marluse.financeiro.service;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.financeiro.dto.LancamentoAtualizarRequest;
import com.example.marluse.financeiro.dto.LancamentoFinanceiroRequest;
import com.example.marluse.financeiro.dto.LancamentoFinanceiroResponse;
import com.example.marluse.financeiro.dto.ResumoDiaResponse;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import com.example.marluse.financeiro.repository.AbatimentoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.vendas.model.Pedido;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LancamentoFinanceiroService {

    private final LancamentoFinanceiroRepository lancamentoFinanceiroRepository;
    private final ClienteRepository clienteRepository;
    private final AbatimentoRepository abatimentoRepository;
    private final AbatimentoService abatimentoService;

    @Transactional
    public LancamentoFinanceiroResponse criar(LancamentoFinanceiroRequest request) {
        Cliente cliente = null;
        if (request.clienteId() != null) {
            cliente = clienteRepository.findById(request.clienteId())
                    .orElseThrow(() -> new EntityNotFoundException("Cliente não encontrado"));
        }

        LancamentoFinanceiro lancamento = LancamentoFinanceiro.builder()
                .tipo(request.tipo())
                .categoria(request.categoria())
                .descricao(request.descricao())
                .valor(request.valor())
                .dataVencimento(request.dataVencimento())
                .dataPagamento(request.dataPagamento())
                .status(request.status() != null ? request.status() : StatusLancamento.PENDENTE)
                .cliente(cliente)
                .build();

        if (request.recorrencia() != null) {
            lancamento.setRecorrencia(request.recorrencia());
            lancamento.setRecorrenciaGrupoId(UUID.randomUUID().toString());
        }

        LancamentoFinanceiro salvo = lancamentoFinanceiroRepository.save(lancamento);

        return LancamentoFinanceiroResponse.from(salvo);
    }

    public List<LancamentoFinanceiroResponse> listarTodos(){
        return lancamentoFinanceiroRepository.findAll()
                .stream()
                .map(LancamentoFinanceiroResponse::from)
                .toList();
    }

    public LancamentoFinanceiroResponse listarPorId(String id){
        return lancamentoFinanceiroRepository.findById(id)
                .map(LancamentoFinanceiroResponse::from)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento Financeiro não encontrado!"));
    }

    public List<LancamentoFinanceiroResponse> listarPendentes(){
        return lancamentoFinanceiroRepository.findByStatus(StatusLancamento.PENDENTE)
                .stream()
                .map(this::toResponse)
                .toList();

    }

    public List<LancamentoFinanceiroResponse> listarVencidos() {
        return lancamentoFinanceiroRepository.findVencidos(LocalDate.now())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<LancamentoFinanceiroResponse> listarLancamentosGrupoId(String grupoId) {
        return  lancamentoFinanceiroRepository.findByRecorrenciaGrupoId(grupoId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public LancamentoFinanceiroResponse pagar(String id){
        LancamentoFinanceiro lancamento = lancamentoFinanceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento Financeiro não encontrado"));

        if (lancamento.getStatus() == StatusLancamento.PAGO){
            throw new IllegalArgumentException( "Esse lançamento já está pago");
        }

        // Parcela já parcialmente abatida: o saldo restante precisa virar um Abatimento, senão
        // ficaria fora tanto da soma por valor (excluída por valorPago > 0) quanto dos abatimentos.
        if (lancamento.getValorPago().signum() > 0 && lancamento.getCliente() != null) {
            abatimentoService.registrarQuitacao(lancamento, "Quitação de saldo em aberto");
        }

        lancamento.setStatus(StatusLancamento.PAGO);
        lancamento.setDataPagamento(LocalDate.now());
        lancamentoFinanceiroRepository.save(lancamento);

        return LancamentoFinanceiroResponse.from(lancamento);
    }

    private LancamentoFinanceiro criarProximoLancamento(LancamentoFinanceiro base, LocalDate data) {
        return LancamentoFinanceiro.builder()
                .tipo(base.getTipo())
                .categoria(base.getCategoria())
                .descricao(base.getDescricao())
                .valor(base.getValor())
                .status(StatusLancamento.PENDENTE)
                .dataVencimento(data)
                .recorrencia(base.getRecorrencia())
                .recorrenciaGrupoId(base.getRecorrenciaGrupoId())
                .recorrenciaAtiva(true)
                .cliente(base.getCliente())
                .build();
    }

    private LocalDate calcularProximaData(LancamentoFinanceiro lancamento) {
        return switch (lancamento.getRecorrencia()) {
            case DIARIA  -> lancamento.getDataVencimento().plusDays(1);
            case SEMANAL -> lancamento.getDataVencimento().plusWeeks(1);
            case MENSAL  -> lancamento.getDataVencimento().plusMonths(1);
            case ANUAL   -> lancamento.getDataVencimento().plusYears(1);
        };
    }


    public ResumoDiaResponse resumoDia(){
        LocalDate hoje = LocalDate.now();
        // Receita do dia = parcelas quitadas à vista (valorPago = 0) + abatimentos recebidos hoje.
        // As duas fontes são mutuamente exclusivas, então não há dupla contagem.
        BigDecimal receitas = lancamentoFinanceiroRepository.somarPorTipoEData(TipoLancamento.RECEITA, hoje)
                .add(abatimentoRepository.somarAbatimentosPorPeriodo(hoje, hoje));
        BigDecimal despesas = lancamentoFinanceiroRepository.somarPorTipoEData(TipoLancamento.DESPESA, hoje);

        BigDecimal saldo = receitas.subtract(despesas);

        return new ResumoDiaResponse(hoje, receitas, despesas, saldo);

    }


    @Transactional
    public LancamentoFinanceiroResponse atualizar(String id, LancamentoAtualizarRequest request) {
        LancamentoFinanceiro lancamento = buscarEntidade(id);

        if (request.tipo()           != null) lancamento.setTipo(request.tipo());
        if (request.categoria()      != null) lancamento.setCategoria(request.categoria());
        if (request.descricao()      != null) lancamento.setDescricao(request.descricao());
        if (request.valor()          != null) lancamento.setValor(request.valor());
        if (request.status()         != null) lancamento.setStatus(request.status());
        if (request.dataVencimento() != null) lancamento.setDataVencimento(request.dataVencimento());
        if (request.dataPagamento()  != null) lancamento.setDataPagamento(request.dataPagamento());

        return LancamentoFinanceiroResponse.from(lancamentoFinanceiroRepository.save(lancamento));
    }

    @Transactional
    public void deletar(String id) {
        LancamentoFinanceiro lancamento = buscarEntidade(id);

        // Lançamentos gerados pelo sistema (vinculados a pedido ou locação) não podem ser
        // deletados diretamente — use cancelar/devolver no pedido/locação pai.
        if (lancamento.getPedido() != null) {
            throw new IllegalArgumentException(
                    "Lançamentos vinculados a uma venda não podem ser excluídos diretamente. Cancele a venda.");
        }
        if (lancamento.getLocacao() != null) {
            throw new IllegalArgumentException(
                    "Lançamentos vinculados a uma locação não podem ser excluídos diretamente. Cancele a locação.");
        }

        lancamentoFinanceiroRepository.delete(lancamento);
    }


    public void registrarVendaReceita(Pedido pedido, String descricao, BigDecimal valor,
                                      StatusLancamento status, LocalDate dataVencimento,
                                      int numeroParcelas) {

        BigDecimal valorParcela = valor.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);

        for (int i = 1 ; i <= numeroParcelas; i++) {
            LocalDate vencimento = dataVencimento.plusMonths(i - 1);
            String desc = numeroParcelas > 1
                    ? String.format("%s (%d/%d)", descricao, i, numeroParcelas)
                    : descricao;


            LancamentoFinanceiro l = LancamentoFinanceiro.builder()
                    .tipo(TipoLancamento.RECEITA)
                    .categoria("Venda")
                    .descricao(desc)
                    .valor(valorParcela)
                    .status(status)
                    .dataVencimento(vencimento)
                    .dataPagamento(status == StatusLancamento.PAGO ? LocalDate.now() : null)
                    .cliente(pedido.getCliente())
                    .pedido(pedido)
                    .numParcelas(i)
                    .totalParcelas(numeroParcelas)
                    .build();

            lancamentoFinanceiroRepository.save(l);
        }

    }

    public void registarLocacaoReceita(Locacao locacao, String descricao, BigDecimal valor,
                                       StatusLancamento status, LocalDate dataVencimento, int numeroParcelas) {

        BigDecimal valorParcela = valor.divide(BigDecimal.valueOf(numeroParcelas), 2, RoundingMode.HALF_UP);

        for (int i = 1; i <= numeroParcelas; i ++ ) {
            LocalDate vencimento = dataVencimento.plusMonths( i - 1);
            String desc = numeroParcelas > 1
                    ? String.format("%s (%d/%d)", descricao, i, numeroParcelas)
                    : descricao;


            LancamentoFinanceiro lancamento = LancamentoFinanceiro.builder()
                    .tipo(TipoLancamento.RECEITA)
                    .categoria("Locação")
                    .descricao(desc)
                    .valor(valorParcela)
                    .status(status)
                    .dataVencimento(vencimento)
                    .dataPagamento(status == StatusLancamento.PAGO ? LocalDate.now() : null)
                    .cliente(locacao.getCliente())
                    .locacao(locacao)
                    .numParcelas(i)
                    .totalParcelas(numeroParcelas)
                    .build();

            lancamentoFinanceiroRepository.save(lancamento);
        }

    }




    @Transactional
    public void cancelarRecorrencia(String grupoId) {
        List<LancamentoFinanceiro> lancamentos = lancamentoFinanceiroRepository
                .findByRecorrenciaGrupoId(grupoId);

        List<LancamentoFinanceiro> paraDesativar = lancamentos.stream()
                .filter(l -> l.getStatus() != StatusLancamento.PENDENTE)
                .peek(l -> l.setRecorrenciaAtiva(false))
                .toList();

        List<LancamentoFinanceiro> paraDeletar = lancamentos.stream()
                .filter(l -> l.getStatus() == StatusLancamento.PENDENTE)
                .toList();

        lancamentoFinanceiroRepository.saveAll(paraDesativar);
        lancamentoFinanceiroRepository.deleteAll(paraDeletar);
    }

    private LancamentoFinanceiro buscarEntidade(String id) {
        return lancamentoFinanceiroRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lançamento não encontrado: " + id));
    }

    private LancamentoFinanceiroResponse toResponse(LancamentoFinanceiro l) {
        return LancamentoFinanceiroResponse.from(l);
    }


}
