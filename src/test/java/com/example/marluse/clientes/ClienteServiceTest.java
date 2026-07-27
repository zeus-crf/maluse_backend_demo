package com.example.marluse.clientes;

import com.example.marluse.clientes.dto.ClienteRequest;
import com.example.marluse.clientes.dto.ClienteResponse;
import com.example.marluse.clientes.model.Cliente;
import com.example.marluse.clientes.repository.ClienteRepository;
import com.example.marluse.clientes.service.ClienteService;
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
public class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    @BeforeEach
    void setUp() {
        clienteRepository.deleteAll();
    }


    @Test
    void deveCriarClienteComSucesso() {
        ClienteRequest request = new ClienteRequest("João Silva", "200.406.789-00", "(31) 99999-9999", "joao@email.com", "Rua A, 1", false);

        ClienteResponse response = clienteService.criar(request);

        assertNotNull(response.id());
        assertEquals("João Silva", response.nome());
        assertEquals("200.406.789-00", response.cpfCnpj());
        assertTrue(response.ativo());
    }

    @Test
    void deveLancarExcecaoAoCriarClineteComCpfDuplicado(){
        ClienteRequest request = new ClienteRequest("João Silva", "123.456.789-00", "(31) 99999-9999", "joao@email.com", "Rua A, 1", false);

        ClienteResponse response = clienteService.criar(request);

        ClienteRequest requestDuplicado = new ClienteRequest("Felipe", "123.456.789-00", "(31) 99999-9329", "felipe@email.com", "Rua B, 1", false);

        assertThrows(IllegalArgumentException.class, () -> clienteService.criar(requestDuplicado));

    }

    @Test
    void deveListarClientesAtivos (){
        clienteService.criar(new ClienteRequest("Cliente 1", "111.111.111-11", null, null, null, false));
        clienteService.criar(new ClienteRequest("Cliente 2", "222.222.222-22", null, null, null, false));

        List<ClienteResponse> clientes = clienteService.listar();

        assertEquals(2, clientes.size());
    }


    @Test
    void deveBuscarClientePorId(){
       ClienteResponse criado = clienteService.criar(new ClienteRequest("João Silva", "123.456.789-00", "(31) 99999-9999", "joao@email.com", "Rua A, 1", false));

        ClienteResponse encontrado = clienteService.listarPorId(criado.id());

        assertEquals(criado.id(), encontrado.id());
        assertEquals(criado.nome(), encontrado.nome());
    }

    @Test
    void deveLancarExcecaoAoBuscarClienteInexistente() {
        assertThrows(EntityNotFoundException.class, () -> clienteService.listarPorId("id-inexistente"));
    }


    @Test
    void deveInativarCliente() {
        ClienteResponse criado = clienteService.criar(new ClienteRequest("Carlos", "444.444.444-44", null, null, null, false));

        clienteService.inativar(criado.id());

        ClienteResponse inativado = clienteService.listarPorId(criado.id());
        assertFalse(inativado.ativo());
    }
}
