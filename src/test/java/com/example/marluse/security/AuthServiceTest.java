package com.example.marluse.security;

import com.example.marluse.security.dto.AuthRequest;
import com.example.marluse.security.dto.RegisterRequest;
import com.example.marluse.security.dto.UsuarioResponse;
import com.example.marluse.security.service.AuthService;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void deveRegistrarUsuarioComSucesso(){
        RegisterRequest request = new RegisterRequest("João", "joao@test.com", "senha123");
        UsuarioResponse response = authService.register(request, new MockHttpServletResponse());

        assertEquals("joao@test.com", response.email());
        assertEquals("João", response.nome());
    }

    @Test
    void deveLancarExcecaoAoRegistrarEmailDuplicado(){
        RegisterRequest request = new RegisterRequest("João", "joao@test.com", "senha123");
        authService.register(request, new MockHttpServletResponse());

        assertThrows(IllegalArgumentException.class,
                () -> authService.register(request, new MockHttpServletResponse()));
    }

    @Test
    void deveRealizarLoginComSucesso(){
        RegisterRequest request = new RegisterRequest("João", "joao@test.com", "senha123");
        authService.register(request, new MockHttpServletResponse());

        AuthRequest loginRequest = new AuthRequest("joao@test.com", "senha123");
        UsuarioResponse response = authService.login(loginRequest, new MockHttpServletResponse());

        assertEquals("joao@test.com", response.email());
    }

    @Test
    void deveLancarExcecaoAoFazerLoginComSenhaErrada(){
        RegisterRequest request = new RegisterRequest("João", "joao@test.com", "senha123");
        authService.register(request, new MockHttpServletResponse());

        AuthRequest authRequest = new AuthRequest("joao@test.com", "senha1234");

        assertThrows(Exception.class,
                () -> authService.login(authRequest, new MockHttpServletResponse()));
    }
}
