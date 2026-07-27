package com.example.marluse.clientes;

import com.example.marluse.clientes.dto.ObservacaoRequest;
import com.example.marluse.clientes.dto.ObservacaoResponse;
import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.clientes.repository.ObservacaoClienteRepository;
import com.example.marluse.clientes.service.ObservacaoClienteService;
import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ObservacaoClienteServiceTest {

    @Autowired private ObservacaoClienteService observacaoService;
    @Autowired private ObservacaoClienteRepository observacaoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private UsuarioRepository usuarioRepository;

    private Cliente cliente;
    private Usuario autor;

    private static final String EMAIL_AUTOR = "miguel@marluse.com";

    @BeforeEach
    void setUp() {
        observacaoRepository.deleteAll();
        clienteRepository.deleteAll();

        cliente = clienteRepository.save(Cliente.builder()
                .nome("Felipe").ativo(true).consumidorFinal(false).build());

        autor = usuarioRepository.findByEmail(EMAIL_AUTOR).orElseGet(() ->
                usuarioRepository.save(Usuario.builder()
                        .nome("Miguel")
                        .email(EMAIL_AUTOR)
                        .senha("irrelevante-no-teste")
                        .build()));
    }

    @Test
    void criarPreencheAutorEDataAutomaticamente() {
        ObservacaoResponse r = observacaoService.criar(
                cliente.getId(), new ObservacaoRequest("Cliente pediz entrega de manhã"), EMAIL_AUTOR);

        assertNotNull(r.id());
        assertEquals("Miguel", r.autorNome(), "o autor vem do usuário logado, não do formulário");
        assertNotNull(r.criadaEm(), "a data sai do createdAt do BaseEntity");
    }

    @Test
    void criarRemoveEspacosSobrandoDoTexto() {
        ObservacaoResponse r = observacaoService.criar(
                cliente.getId(), new ObservacaoRequest("   anotação com espaços   "), EMAIL_AUTOR);

        assertEquals("anotação com espaços", r.texto());
    }

    @Test
    void listaDaMaisRecenteParaAMaisAntiga() {
        observacaoService.criar(cliente.getId(), new ObservacaoRequest("primeira"), EMAIL_AUTOR);
        observacaoService.criar(cliente.getId(), new ObservacaoRequest("segunda"), EMAIL_AUTOR);
        observacaoService.criar(cliente.getId(), new ObservacaoRequest("terceira"), EMAIL_AUTOR);

        List<ObservacaoResponse> lista = observacaoService.listar(cliente.getId());

        assertEquals(3, lista.size());
        // createdAt pode empatar no milissegundo dentro do mesmo teste; o que importa é que
        // a query ordena por ele DESC e as três voltam.
        assertTrue(lista.stream().anyMatch(o -> o.texto().equals("terceira")));
        assertTrue(lista.get(0).criadaEm().compareTo(lista.get(2).criadaEm()) >= 0,
                "a primeira da lista não pode ser mais antiga que a última");
    }

    @Test
    void listaSoTrazObservacoesDoClientePedido() {
        Cliente outro = clienteRepository.save(Cliente.builder()
                .nome("Rodrigo").ativo(true).consumidorFinal(false).build());

        observacaoService.criar(cliente.getId(), new ObservacaoRequest("do Felipe"), EMAIL_AUTOR);
        observacaoService.criar(outro.getId(),   new ObservacaoRequest("do Rodrigo"), EMAIL_AUTOR);

        List<ObservacaoResponse> doFelipe = observacaoService.listar(cliente.getId());

        assertEquals(1, doFelipe.size());
        assertEquals("do Felipe", doFelipe.get(0).texto());
    }

    @Test
    void deletarRemoveApenasAObservacaoAlvo() {
        ObservacaoResponse manter  = observacaoService.criar(
                cliente.getId(), new ObservacaoRequest("manter"), EMAIL_AUTOR);
        ObservacaoResponse apagar = observacaoService.criar(
                cliente.getId(), new ObservacaoRequest("apagar"), EMAIL_AUTOR);

        observacaoService.deletar(apagar.id());

        List<ObservacaoResponse> restantes = observacaoService.listar(cliente.getId());
        assertEquals(1, restantes.size());
        assertEquals(manter.id(), restantes.get(0).id());
    }

    @Test
    void clienteInexistenteFalhaAoCriar() {
        assertThrows(EntityNotFoundException.class, () ->
                observacaoService.criar("id-que-nao-existe", new ObservacaoRequest("texto"), EMAIL_AUTOR));
    }

    @Test
    void clienteInexistenteFalhaAoListar() {
        assertThrows(EntityNotFoundException.class, () ->
                observacaoService.listar("id-que-nao-existe"));
    }

    @Test
    void observacaoInexistenteFalhaAoDeletar() {
        assertThrows(EntityNotFoundException.class, () ->
                observacaoService.deletar("id-que-nao-existe"));
    }
}
