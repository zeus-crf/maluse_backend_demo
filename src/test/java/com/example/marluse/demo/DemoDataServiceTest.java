package com.example.marluse.demo;

import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.estoque.repository.FornecedorRepository;
import com.example.marluse.estoque.repository.ProdutoRepository;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import com.example.marluse.locacoes.repository.LocacaoRepository;
import com.example.marluse.security.repository.UsuarioRepository;
import com.example.marluse.vendas.repository.PedidoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DemoDataServiceTest {

    @Autowired DemoDataService demoDataService;
    @Autowired ClienteRepository clienteRepository;
    @Autowired FornecedorRepository fornecedorRepository;
    @Autowired ProdutoRepository produtoRepository;
    @Autowired PedidoRepository pedidoRepository;
    @Autowired LocacaoRepository locacaoRepository;
    @Autowired LancamentoFinanceiroRepository lancamentoRepository;
    @Autowired UsuarioRepository usuarioRepository;

    // H2 é compartilhado entre os métodos (DB_CLOSE_DELAY=-1) e o seed é determinístico,
    // então cada teste precisa partir de um estado limpo para não colidir CPF/número.
    @BeforeEach
    void limpar() {
        demoDataService.clear();
    }

    @Test
    void seed_populaTodosOsModulos() {
        demoDataService.clear();
        demoDataService.seed();

        assertTrue(clienteRepository.count() > 0, "clientes");
        assertTrue(fornecedorRepository.count() > 0, "fornecedores");
        assertTrue(produtoRepository.count() > 0, "produtos");
        assertTrue(pedidoRepository.count() > 0, "pedidos");
        assertTrue(locacaoRepository.count() > 0, "locacoes");
        assertTrue(lancamentoRepository.count() > 0, "lancamentos");
    }

    @Test
    void clear_apagaNegocioMasPreservaUsuarios() {
        demoDataService.seed();
        long usuariosAntes = usuarioRepository.count();

        demoDataService.clear();

        assertEquals(0, clienteRepository.count(), "clientes");
        assertEquals(0, produtoRepository.count(), "produtos");
        assertEquals(0, pedidoRepository.count(), "pedidos");
        assertEquals(0, locacaoRepository.count(), "locacoes");
        assertEquals(0, lancamentoRepository.count(), "lancamentos");
        assertEquals(usuariosAntes, usuarioRepository.count(), "usuarios preservados");
    }
}
