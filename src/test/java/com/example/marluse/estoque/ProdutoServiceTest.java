package com.example.marluse.estoque;

import com.example.marluse.estoque.dto.CategoriaProduto;
import com.example.marluse.estoque.dto.ProdutoAtualizarRequest;
import com.example.marluse.estoque.dto.ProdutoFornecedorRequest;
import com.example.marluse.estoque.dto.ProdutoFornecedorResponse;
import com.example.marluse.estoque.dto.ProdutoRequest;
import com.example.marluse.estoque.dto.ProdutoResponse;
import com.example.marluse.estoque.enums.UnidadeMedida;
import com.example.marluse.estoque.repository.ProdutoRepository;
import com.example.marluse.estoque.service.ProdutoService;
import com.example.marluse.estoque.repository.FornecedorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ProdutoServiceTest {

    @Autowired
    private ProdutoService produtoService;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @BeforeEach
    void setUp(){
        produtoRepository.deleteAll();
        fornecedorRepository.deleteAll();
    }

    private ProdutoRequest produtoValido(String nome, int quantidade) {
        return produtoValido(nome, quantidade, List.of());
    }

    private ProdutoRequest produtoValido(String nome, int quantidade, List<ProdutoFornecedorRequest> fornecedores) {
        return new ProdutoRequest(
                nome, "Descrição",
                new BigDecimal("10.00"),   // valorCompra
                new BigDecimal("25.00"),   // preco
                new BigDecimal("5.00"),    // precoDiaria
                BigDecimal.valueOf(quantidade), 5,
                UnidadeMedida.SACO,
                CategoriaProduto.OUTROS,
                fornecedores);
    }

    /** Atalho para uma linha fornecedor+preço. */
    private ProdutoFornecedorRequest pf(String nome, String preco) {
        return new ProdutoFornecedorRequest(nome, preco == null ? null : new BigDecimal(preco));
    }

    /** Nomes dos fornecedores da resposta, na ordem em que vieram. */
    private List<String> nomes(ProdutoResponse r) {
        return r.fornecedores().stream().map(ProdutoFornecedorResponse::nome).toList();
    }

    /** Preço do fornecedor de nome informado, ou null se ausente. */
    private BigDecimal precoDe(ProdutoResponse r, String nome) {
        return r.fornecedores().stream()
                .filter(f -> f.nome().equals(nome))
                .findFirst()
                .map(ProdutoFornecedorResponse::precoCompra)
                .orElse(null);
    }

    @Test
    void deveCriarProdutoComSucesso() {
        ProdutoResponse response = produtoService.criar(produtoValido("Cimento", 50));

        assertNotNull(response.id());
        assertEquals("Cimento", response.nome());
        assertEquals(0, response.quantidadeEstoque().compareTo(BigDecimal.valueOf(50)));
        assertTrue(response.ativo());
    }

    @Test
    void deveListarApenasAtivos() {
        ProdutoResponse p1 = produtoService.criar(produtoValido("Cimento", 50));
        ProdutoResponse p2 = produtoService.criar(produtoValido("Areia", 30));
        produtoService.inativar(p2.id());

        List<ProdutoResponse> ativos = produtoService.listar();

        assertEquals(1, ativos.size());
        assertEquals("Cimento", ativos.get(0).nome());
    }

    @Test
    void deveIdentificarEstoqueBaixo() {
        produtoService.criar(produtoValido("Cimento", 3)); // abaixo do mínimo (5)
        produtoService.criar(produtoValido("Areia", 20)); // acima do mínimo

        List<ProdutoResponse> resultado = produtoService.listarEstoqueBaixo();

        assertEquals(1, resultado.size());
        assertTrue(resultado.get(0).estoqueBaixo());
    }

    @Test
    void deveAtualizarProduto() {
        ProdutoResponse criado = produtoService.criar(produtoValido("Cimento", 50));

        ProdutoAtualizarRequest atualizado = new ProdutoAtualizarRequest(
                "Cimento CP-II", "Nova descrição",
                new BigDecimal("12.00"),   // valorCompra
                new BigDecimal("30.00"),   // preco
                new BigDecimal("6.00"),    // precoDiaria
                BigDecimal.valueOf(50), 10,
                UnidadeMedida.SACO,
                CategoriaProduto.OUTROS,
                null);   // null = não mexe nos fornecedores
        ProdutoResponse response = produtoService.atualizar(criado.id(), atualizado);

        assertEquals("Cimento CP-II", response.nome());
        assertEquals(new BigDecimal("30.00"), response.preco());
    }

    @Test
    void deveLancarExcecaoAoBuscarProdutoInexistente() {
        assertThrows(EntityNotFoundException.class, () -> produtoService.burcarPorId("id-inexistente"));
    }

    @Test
    void deveInativarProduto() {
        ProdutoResponse criado = produtoService.criar(produtoValido("Cimento", 50));

        produtoService.inativar(criado.id());

        ProdutoResponse inativado = produtoService.burcarPorId(criado.id());
        assertFalse(inativado.ativo());
    }

    // --- fornecedores -------------------------------------------------------

    @Test
    void deveCriarProdutoComFornecedoresEPrecos() {
        ProdutoResponse response = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", "10.00"), pf("Tigre", "8.50"))));

        assertEquals(List.of("Tigre", "Votorantim"), nomes(response), "ordenado por nome");
        assertEquals(0, new BigDecimal("10.00").compareTo(precoDe(response, "Votorantim")));
        assertEquals(0, new BigDecimal("8.50").compareTo(precoDe(response, "Tigre")));
        assertEquals(2, fornecedorRepository.count());
    }

    @Test
    void devePermitirPrecoNuloNoVinculo() {
        ProdutoResponse response = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", null))));

        assertEquals(List.of("Votorantim"), nomes(response));
        assertNull(precoDe(response, "Votorantim"));
    }

    @Test
    void deveGuardarPrecosDiferentesParaOMesmoFornecedorEmProdutosDiferentes() {
        ProdutoResponse cimento = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", "10.00"))));
        ProdutoResponse reboco = produtoService.criar(
                produtoValido("Reboco", 30, List.of(pf("votorantim", "8.00"))));

        assertEquals(0, new BigDecimal("10.00").compareTo(precoDe(cimento, "Votorantim")));
        assertEquals(0, new BigDecimal("8.00").compareTo(precoDe(reboco, "Votorantim")));
        assertEquals(1, fornecedorRepository.count(), "é o mesmo fornecedor, um só cadastro");
    }

    @Test
    void deveCriarProdutoSemFornecedores() {
        ProdutoResponse response = produtoService.criar(produtoValido("Cimento", 50));

        assertTrue(response.fornecedores().isEmpty());
    }

    @Test
    void deveSubstituirOsFornecedoresNoUpdate() {
        ProdutoResponse criado = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", "10.00"))));

        ProdutoResponse atualizado = produtoService.atualizar(criado.id(),
                atualizarComFornecedores(List.of(pf("Tigre", "8.00"))));

        assertEquals(List.of("Tigre"), nomes(atualizado));
        assertEquals(0, new BigDecimal("8.00").compareTo(precoDe(atualizado, "Tigre")));
    }

    @Test
    void deveLimparOsFornecedoresComListaVazia() {
        ProdutoResponse criado = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", "10.00"))));

        ProdutoResponse atualizado = produtoService.atualizar(criado.id(),
                atualizarComFornecedores(List.of()));

        assertTrue(atualizado.fornecedores().isEmpty());
    }

    @Test
    void devePreservarOsFornecedoresQuandoOCampoVemNulo() {
        ProdutoResponse criado = produtoService.criar(
                produtoValido("Cimento", 50, List.of(pf("Votorantim", "10.00"))));

        ProdutoResponse atualizado = produtoService.atualizar(criado.id(),
                atualizarComFornecedores(null));

        assertEquals(List.of("Votorantim"), nomes(atualizado));
        assertEquals(0, new BigDecimal("10.00").compareTo(precoDe(atualizado, "Votorantim")));
    }

    private ProdutoAtualizarRequest atualizarComFornecedores(List<ProdutoFornecedorRequest> fornecedores) {
        return new ProdutoAtualizarRequest(
                "Cimento", "Descrição",
                new BigDecimal("10.00"),
                new BigDecimal("25.00"),
                new BigDecimal("5.00"),
                BigDecimal.valueOf(50), 5,
                UnidadeMedida.SACO,
                CategoriaProduto.OUTROS,
                fornecedores);
    }
}
