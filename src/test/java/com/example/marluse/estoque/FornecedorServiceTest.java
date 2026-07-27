package com.example.marluse.estoque;

import com.example.marluse.estoque.model.Fornecedor;
import com.example.marluse.estoque.repository.FornecedorRepository;
import com.example.marluse.estoque.service.FornecedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Sem @Transactional na classe de propósito: `resolverPorNomes` é transacional e
 * precisa commitar de verdade para as asserções de `count()` refletirem o banco.
 */
@SpringBootTest
@ActiveProfiles("test")
public class FornecedorServiceTest {

    @Autowired
    private FornecedorRepository fornecedorRepository;

    @Autowired
    private FornecedorService fornecedorService;

    @BeforeEach
    void setUp() {
        fornecedorRepository.deleteAll();
    }

    // --- repository ---------------------------------------------------------

    @Test
    void deveEncontrarFornecedorIgnorandoCaixa() {
        fornecedorRepository.save(Fornecedor.builder().nome("Votorantim").build());

        Optional<Fornecedor> achado = fornecedorRepository.findByNomeIgnoreCase("VOTORANTIM");

        assertTrue(achado.isPresent());
        assertEquals("Votorantim", achado.get().getNome());
    }

    @Test
    void deveListarApenasAtivosEmOrdemAlfabetica() {
        fornecedorRepository.save(Fornecedor.builder().nome("Tigre").build());
        fornecedorRepository.save(Fornecedor.builder().nome("Amanco").build());
        fornecedorRepository.save(Fornecedor.builder().nome("Inativo").ativo(false).build());

        var ativos = fornecedorRepository.findByAtivoTrueOrderByNomeAsc();

        assertEquals(2, ativos.size());
        assertEquals("Amanco", ativos.get(0).getNome());
        assertEquals("Tigre", ativos.get(1).getNome());
    }

    // --- resolverPorNomes ---------------------------------------------------

    @Test
    void deveCriarFornecedorQuandoNomeNaoExiste() {
        Set<Fornecedor> resolvidos = fornecedorService.resolverPorNomes(List.of("Votorantim"));

        assertEquals(1, resolvidos.size());
        assertEquals("Votorantim", resolvidos.iterator().next().getNome());
        assertEquals(1, fornecedorRepository.count());
    }

    @Test
    void deveReutilizarFornecedorExistenteIgnorandoCaixaEEspacos() {
        fornecedorRepository.save(Fornecedor.builder().nome("Votorantim").build());

        Set<Fornecedor> resolvidos = fornecedorService.resolverPorNomes(List.of("  votorantim  "));

        assertEquals(1, resolvidos.size());
        assertEquals("Votorantim", resolvidos.iterator().next().getNome());
        assertEquals(1, fornecedorRepository.count(), "não deve ter criado um segundo fornecedor");
    }

    @Test
    void deveDeduplicarNomesRepetidosNaMesmaRequisicao() {
        Set<Fornecedor> resolvidos =
                fornecedorService.resolverPorNomes(List.of("Tigre", "TIGRE", " tigre "));

        assertEquals(1, resolvidos.size());
        assertEquals(1, fornecedorRepository.count());
    }

    @Test
    void devePreservarAGrafiaDaPrimeiraOcorrencia() {
        Set<Fornecedor> resolvidos =
                fornecedorService.resolverPorNomes(List.of("Votorantim", "VOTORANTIM"));

        assertEquals("Votorantim", resolvidos.iterator().next().getNome());
    }

    @Test
    void deveDescartarNomesVaziosENulos() {
        List<String> nomes = new ArrayList<>(List.of("Tigre", "   ", ""));
        nomes.add(null);

        Set<Fornecedor> resolvidos = fornecedorService.resolverPorNomes(nomes);

        assertEquals(1, resolvidos.size());
        assertEquals("Tigre", resolvidos.iterator().next().getNome());
    }

    @Test
    void deveRetornarConjuntoVazioParaListaNula() {
        assertTrue(fornecedorService.resolverPorNomes(null).isEmpty());
        assertEquals(0, fornecedorRepository.count());
    }

    @Test
    void deveTruncarNomeAcimaDoLimite() {
        String longo = "A".repeat(200);

        Set<Fornecedor> resolvidos = fornecedorService.resolverPorNomes(List.of(longo));

        assertEquals(120, resolvidos.iterator().next().getNome().length());
    }

    // --- listar -------------------------------------------------------------

    @Test
    void deveListarFornecedoresAtivos() {
        fornecedorService.resolverPorNomes(List.of("Tigre", "Amanco"));

        var lista = fornecedorService.listar();

        assertEquals(2, lista.size());
        assertEquals("Amanco", lista.get(0).nome());
        assertNotNull(lista.get(0).id());
    }
}
