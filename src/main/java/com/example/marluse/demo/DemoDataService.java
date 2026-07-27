package com.example.marluse.demo;

import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.entrega.enums.StatusEntrega;
import com.example.marluse.entrega.model.Entrega;
import com.example.marluse.estoque.dto.CategoriaProduto;
import com.example.marluse.estoque.enums.UnidadeMedida;
import com.example.marluse.estoque.model.Fornecedor;
import com.example.marluse.estoque.model.Produto;
import com.example.marluse.estoque.model.ProdutoFornecedor;
import com.example.marluse.estoque.repository.FornecedorRepository;
import com.example.marluse.estoque.repository.ProdutoRepository;
import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.enums.TipoLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.locacoes.model.ItemLocacao;
import com.example.marluse.locacoes.model.Locacao;
import com.example.marluse.locacoes.repository.LocacaoRepository;
import com.example.marluse.vendas.enums.FormaPagamento;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.model.ItemPedido;
import com.example.marluse.vendas.model.Pedido;
import com.example.marluse.vendas.repository.PedidoRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
public class DemoDataService {

    @PersistenceContext
    private EntityManager em;

    private final DemoProperties props;
    private final ClienteRepository clienteRepository;
    private final FornecedorRepository fornecedorRepository;
    private final ProdutoRepository produtoRepository;
    private final PedidoRepository pedidoRepository;
    private final LocacaoRepository locacaoRepository;
    private final LancamentoFinanceiroRepository lancamentoRepository;

    // Random determinístico → o demo sempre tem a mesma "cara" após cada reset.
    private final Random rng = new Random(42);

    // ============================================================
    // CLEAR
    // ============================================================

    /**
     * Apaga todos os dados de negócio na ordem folha→raiz (respeitando FKs).
     * NÃO apaga a tabela usuarios (login do demo é preservado).
     */
    @Transactional
    public void clear() {
        // Ordem importa: filhos antes dos pais.
        bulkDelete("AbatimentoParcela");
        bulkDelete("Abatimento");
        bulkDelete("LancamentoFinanceiro");
        bulkDelete("Entrega");
        bulkDelete("ItemPedido");
        bulkDelete("ItemLocacao");
        bulkDelete("Pedido");
        bulkDelete("Locacao");
        bulkDelete("ProdutoFornecedor");
        bulkDelete("ObservacaoCliente");
        bulkDelete("Produto");
        bulkDelete("Fornecedor");
        bulkDelete("Cliente");
        em.flush();
        em.clear();
        log.info("[Demo] Dados de negócio limpos (usuários preservados).");
    }

    private void bulkDelete(String entityName) {
        em.createQuery("DELETE FROM " + entityName).executeUpdate();
    }

    // ============================================================
    // SEED
    // ============================================================

    @Transactional
    public void seed() {
        rng.setSeed(42); // reprodutível a cada reset
        List<Cliente> clientes = seedClientes();
        List<Fornecedor> fornecedores = seedFornecedores();
        List<Produto> produtos = seedProdutos(fornecedores);
        seedVendas(clientes, produtos);
        seedLocacoes(clientes, produtos);
        seedFinanceiro(clientes);
        log.info("[Demo] Seed concluída: {} clientes, {} fornecedores, {} produtos.",
                clientes.size(), fornecedores.size(), produtos.size());
    }

    // ---- clientes ----

    private static final String[] NOMES = {
        "Construtora Alvorada", "Marcos Pereira", "Ana Beatriz Souza", "Ferragens Central",
        "João Ribeiro", "Casa & Obra Materiais", "Lúcia Fernandes", "Reforma Já Ltda",
        "Pedro Henrique Lima", "Edificar Engenharia", "Mariana Costa", "Gilberto Alves",
        "Obras Premium", "Rafael Monteiro", "Cláudia Nunes", "Vale Construções",
        "Sérgio Batista", "Tânia Rodrigues"
    };
    private static final String[] RUAS = {
        "Rua das Palmeiras", "Av. Brasil", "Rua São João", "Travessa da Paz",
        "Av. Getúlio Vargas", "Rua do Comércio", "Rua Sete de Setembro"
    };

    private List<Cliente> seedClientes() {
        List<Cliente> lista = new ArrayList<>();
        for (int i = 0; i < props.getClientes(); i++) {
            String nome = NOMES[i % NOMES.length] + (i >= NOMES.length ? " " + i : "");
            Cliente c = Cliente.builder()
                    .nome(nome)
                    .cpfCnpj(gerarCpfCnpj(i))
                    .telefone(gerarTelefone())
                    .email(slug(nome) + "@exemplo.com")
                    .endereco(RUAS[rng.nextInt(RUAS.length)] + ", " + (100 + rng.nextInt(900)))
                    .consumidorFinal(i % 5 == 0)
                    .ativo(true)
                    .build();
            lista.add(c);
        }
        return clienteRepository.saveAll(lista);
    }

    // ---- fornecedores ----

    private static final String[] FORNECEDORES = {
        "Distribuidora Norte", "Aço Forte S.A.", "Elétrica União",
        "Cimento Base", "Tubos & Cia", "Ferramentas MaxPro"
    };

    private List<Fornecedor> seedFornecedores() {
        List<Fornecedor> lista = new ArrayList<>();
        int n = Math.min(props.getFornecedores(), FORNECEDORES.length);
        for (int i = 0; i < n; i++) {
            lista.add(Fornecedor.builder().nome(FORNECEDORES[i]).ativo(true).build());
        }
        return fornecedorRepository.saveAll(lista);
    }

    // ---- produtos ----

    // nome, categoria, unidade, precoVenda, precoDiaria(nullable p/ não-locação)
    private record ProdutoSeed(String nome, CategoriaProduto cat, UnidadeMedida un, double preco, Double diaria) {}

    private static final ProdutoSeed[] CATALOGO = {
        new ProdutoSeed("Cimento CP-II 50kg", CategoriaProduto.ENSACADOS, UnidadeMedida.SACO, 32.90, null),
        new ProdutoSeed("Areia média", CategoriaProduto.MATERIAL_BRUTO, UnidadeMedida.METRO_QUADRADO, 120.00, null),
        new ProdutoSeed("Brita 1", CategoriaProduto.MATERIAL_BRUTO, UnidadeMedida.METRO_QUADRADO, 135.00, null),
        new ProdutoSeed("Vergalhão 10mm", CategoriaProduto.MATERIAL_BRUTO, UnidadeMedida.PECA, 48.50, null),
        new ProdutoSeed("Cabo flexível 2,5mm", CategoriaProduto.ELETRICA, UnidadeMedida.ROLO, 189.00, null),
        new ProdutoSeed("Disjuntor 20A", CategoriaProduto.ELETRICA, UnidadeMedida.PECA, 18.90, null),
        new ProdutoSeed("Tubo PVC 100mm", CategoriaProduto.CONEXOES_E_TUBOS, UnidadeMedida.METRO, 27.00, null),
        new ProdutoSeed("Joelho 90° 100mm", CategoriaProduto.CONEXOES_E_TUBOS, UnidadeMedida.PECA, 9.50, null),
        new ProdutoSeed("Tinta acrílica 18L", CategoriaProduto.OUTROS, UnidadeMedida.BALDE, 289.00, null),
        new ProdutoSeed("Furadeira de impacto", CategoriaProduto.FERRAMENTAS, UnidadeMedida.PECA, 349.00, null),
        new ProdutoSeed("Betoneira 400L", CategoriaProduto.LOCACAO, UnidadeMedida.PECA, 4200.00, 85.00),
        new ProdutoSeed("Andaime 1,5m (módulo)", CategoriaProduto.LOCACAO, UnidadeMedida.PECA, 890.00, 22.00),
        new ProdutoSeed("Compactador de solo", CategoriaProduto.LOCACAO, UnidadeMedida.PECA, 6800.00, 150.00),
        new ProdutoSeed("Escora metálica", CategoriaProduto.LOCACAO, UnidadeMedida.PECA, 75.00, 4.50),
    };

    private List<Produto> seedProdutos(List<Fornecedor> fornecedores) {
        List<Produto> lista = new ArrayList<>();
        for (int i = 0; i < props.getProdutos(); i++) {
            ProdutoSeed s = CATALOGO[i % CATALOGO.length];
            String nome = s.nome() + (i >= CATALOGO.length ? " (v" + (i / CATALOGO.length + 1) + ")" : "");
            BigDecimal preco = money(s.preco());
            Produto p = Produto.builder()
                    .nome(nome)
                    .descricao("Item de catálogo para demonstração.")
                    .valorCompra(preco.multiply(new BigDecimal("0.65")).setScale(2, RoundingMode.HALF_UP))
                    .preco(preco)
                    .precoDiaria(s.diaria() == null ? null : money(s.diaria()))
                    .quantidadeEstoque(new BigDecimal(5 + rng.nextInt(120)))
                    .estoqueMinimo(5)
                    .ativo(true)
                    .medida(s.un())
                    .categoria(s.cat())
                    .rascunho(false)
                    .build();
            // 1 fornecedor por produto
            Fornecedor f = fornecedores.get(rng.nextInt(fornecedores.size()));
            p.getFornecedores().add(ProdutoFornecedor.builder()
                    .produto(p).fornecedor(f)
                    .precoCompra(p.getValorCompra())
                    .build());
            lista.add(p);
        }
        return produtoRepository.saveAll(lista);
    }

    // ---- vendas (pedidos) ----

    private void seedVendas(List<Cliente> clientes, List<Produto> produtos) {
        List<Produto> vendaveis = produtos.stream()
                .filter(p -> p.getCategoria() != CategoriaProduto.LOCACAO).toList();
        StatusPedido[] statuses = { StatusPedido.PAGO, StatusPedido.CONFIRMADO,
                StatusPedido.PENDENTE, StatusPedido.ORCAMENTO, StatusPedido.CANCELADO };
        FormaPagamento[] formas = { FormaPagamento.PIX, FormaPagamento.DINHEIRO,
                FormaPagamento.CARTAO_CREDITO, FormaPagamento.BOLETO, FormaPagamento.FIADO };

        List<Pedido> pedidos = new ArrayList<>();
        for (int i = 0; i < props.getPedidos(); i++) {
            Cliente cliente = clientes.get(rng.nextInt(clientes.size()));
            StatusPedido status = statuses[weightedStatus()];
            // Espalha os movimentos nos últimos ~120 dias p/ os gráficos mensais.
            LocalDate movimento = LocalDate.now().minusDays(rng.nextInt(120));

            Pedido pedido = Pedido.builder()
                    .numero((long) (1000 + i))
                    .cliente(cliente)
                    .status(status)
                    .formaPagamento(formas[rng.nextInt(formas.length)])
                    .valorTotal(BigDecimal.ZERO)
                    .dataMovimento(movimento)
                    .dataVencimento(movimento.plusDays(30))
                    .estoqueDescontado(status == StatusPedido.PAGO || status == StatusPedido.CONFIRMADO)
                    .observacao(i % 7 == 0 ? "Pedido de demonstração." : null)
                    .build();

            BigDecimal total = BigDecimal.ZERO;
            int nItens = 1 + rng.nextInt(4);
            for (int j = 0; j < nItens; j++) {
                Produto prod = vendaveis.get(rng.nextInt(vendaveis.size()));
                BigDecimal qtd = new BigDecimal(1 + rng.nextInt(10));
                BigDecimal sub = prod.getPreco().multiply(qtd).setScale(2, RoundingMode.HALF_UP);
                pedido.getItens().add(ItemPedido.builder()
                        .pedido(pedido).produto(prod)
                        .quantidade(qtd)
                        .precoUnitario(prod.getPreco())
                        .custoUnitario(prod.getValorCompra())
                        .subTotal(sub)
                        .build());
                total = total.add(sub);
            }
            pedido.setValorTotal(total);

            // ~40% dos pedidos têm entrega (cascade ALL a partir de Pedido)
            if (rng.nextInt(10) < 4) {
                boolean feita = status == StatusPedido.PAGO;
                Entrega entrega = new Entrega();
                entrega.setPedido(pedido);
                entrega.setEndereco(cliente.getEndereco());
                entrega.setDataPrevista(movimento.plusDays(2));
                entrega.setDataEntrega(feita ? movimento.plusDays(2) : null);
                entrega.setStatus(feita ? StatusEntrega.FEITA : StatusEntrega.PENDENTE);
                pedido.setEntrega(entrega);
            }
            pedidos.add(pedido);
        }
        pedidoRepository.saveAll(pedidos);
    }

    // Enviesa para status "bons" (PAGO/CONFIRMADO) na maioria dos casos.
    private int weightedStatus() {
        int r = rng.nextInt(10);
        if (r < 5) return 0;      // PAGO
        if (r < 7) return 1;      // CONFIRMADO
        if (r < 8) return 2;      // PENDENTE
        if (r < 9) return 3;      // ORCAMENTO
        return 4;                 // CANCELADO
    }

    // ---- locações ----

    private void seedLocacoes(List<Cliente> clientes, List<Produto> produtos) {
        List<Produto> locaveis = produtos.stream()
                .filter(p -> p.getCategoria() == CategoriaProduto.LOCACAO && p.getPrecoDiaria() != null)
                .toList();
        if (locaveis.isEmpty()) return;

        List<Locacao> locacoes = new ArrayList<>();
        for (int i = 0; i < props.getLocacoes(); i++) {
            Cliente cliente = clientes.get(rng.nextInt(clientes.size()));
            LocalDate retirada = LocalDate.now().minusDays(rng.nextInt(60));
            int dias = 3 + rng.nextInt(20);
            LocalDate prevista = retirada.plusDays(dias);

            // Distribui status de forma coerente com as datas.
            StatusLocacao status;
            LocalDate devolucaoReal = null;
            int r = rng.nextInt(10);
            if (r < 4) { status = StatusLocacao.ATIVA; }
            else if (r < 7) { status = StatusLocacao.DEVOLVIDA; devolucaoReal = prevista.minusDays(rng.nextInt(3)); }
            else if (r < 9) { status = StatusLocacao.ATRASADA; }
            else { status = StatusLocacao.ORCAMENTO; }
            if (status == StatusLocacao.ATRASADA) prevista = LocalDate.now().minusDays(1 + rng.nextInt(10));

            Locacao loc = Locacao.builder()
                    .numero((long) (2000 + i))
                    .cliente(cliente)
                    .status(status)
                    .formaPagamento(FormaPagamento.PIX)
                    .dataRetirada(retirada)
                    .dataDevolucaoPrevista(prevista)
                    .dataDevolucaoReal(devolucaoReal)
                    .valorTotal(BigDecimal.ZERO)
                    .dataMovimento(retirada)
                    .estoqueDescontado(status == StatusLocacao.ATIVA || status == StatusLocacao.ATRASADA)
                    .build();

            BigDecimal total = BigDecimal.ZERO;
            int nItens = 1 + rng.nextInt(3);
            for (int j = 0; j < nItens; j++) {
                Produto prod = locaveis.get(rng.nextInt(locaveis.size()));
                BigDecimal qtd = new BigDecimal(1 + rng.nextInt(4));
                BigDecimal sub = prod.getPrecoDiaria().multiply(qtd)
                        .multiply(new BigDecimal(dias)).setScale(2, RoundingMode.HALF_UP);
                loc.getItens().add(ItemLocacao.builder()
                        .locacao(loc).produto(prod)
                        .quantidade(qtd)
                        .precoDiaria(prod.getPrecoDiaria())
                        .subtotal(sub)
                        .build());
                total = total.add(sub);
            }
            loc.setValorTotal(total);
            locacoes.add(loc);
        }
        locacaoRepository.saveAll(locacoes);
    }

    // ---- financeiro ----

    private static final String[] CAT_RECEITA = { "Vendas", "Locações", "Serviços" };
    private static final String[] CAT_DESPESA = { "Aluguel", "Energia", "Salários", "Fornecedores", "Combustível", "Impostos" };

    private void seedFinanceiro(List<Cliente> clientes) {
        List<LancamentoFinanceiro> lancamentos = new ArrayList<>();
        for (int i = 0; i < props.getLancamentos(); i++) {
            boolean receita = rng.nextBoolean();
            LocalDate venc = LocalDate.now().minusDays(rng.nextInt(90) - 30); // passado e futuro
            boolean pago = !venc.isAfter(LocalDate.now()) && rng.nextInt(10) < 7;
            StatusLancamento status = pago ? StatusLancamento.PAGO
                    : (venc.isBefore(LocalDate.now()) ? StatusLancamento.VENCIDO : StatusLancamento.PENDENTE);
            BigDecimal valor = money(80 + rng.nextInt(4000) + rng.nextDouble());

            LancamentoFinanceiro l = LancamentoFinanceiro.builder()
                    .tipo(receita ? TipoLancamento.RECEITA : TipoLancamento.DESPESA)
                    .categoria(receita ? CAT_RECEITA[rng.nextInt(CAT_RECEITA.length)]
                                        : CAT_DESPESA[rng.nextInt(CAT_DESPESA.length)])
                    .descricao((receita ? "Recebimento" : "Pagamento") + " #" + (i + 1))
                    .valor(valor)
                    .valorPago(pago ? valor : BigDecimal.ZERO)
                    .dataVencimento(venc)
                    .dataPagamento(pago ? venc : null)
                    .status(status)
                    .recorrenciaAtiva(false)
                    .cliente(receita ? clientes.get(rng.nextInt(clientes.size())) : null)
                    .build();
            lancamentos.add(l);
        }
        lancamentoRepository.saveAll(lancamentos);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private BigDecimal money(double v) {
        return new BigDecimal(Double.toString(v)).setScale(2, RoundingMode.HALF_UP);
    }

    private String slug(String s) {
        return s.toLowerCase().replaceAll("[^a-z0-9]+", ".");
    }

    private String gerarTelefone() {
        return "(11) 9" + (1000 + rng.nextInt(9000)) + "-" + (1000 + rng.nextInt(9000));
    }

    private String gerarCpfCnpj(int i) {
        return String.format("%03d.%03d.%03d-%02d", i + 1, rng.nextInt(1000), rng.nextInt(1000), rng.nextInt(100));
    }
}
