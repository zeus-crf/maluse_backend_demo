package com.example.marluse.security;

import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email}")
    private String adminEmail;

    @Value("${app.admin.senha}")
    private String adminSenha;

    private static final int TAMANHO_MINIMO_SENHA = 8;

    // Senhas que já apareceram como default no histórico do repo
    private static final Set<String> SENHAS_PROIBIDAS = Set.of("admin123", "marluse2024", "admin", "12345678");

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
            validarSenhaAdmin();
            Usuario admin = Usuario.builder()
                    .nome("Administrador")
                    .email(adminEmail)
                    .senha(passwordEncoder.encode(adminSenha))
                    .ativo(true)
                    .build();
            usuarioRepository.save(admin);
            log.info("Usuário admin criado: {}", adminEmail);
        } else {
            log.info("Usuário admin já existe, seed ignorado.");
        }
    }

    private void validarSenhaAdmin() {
        if (adminSenha == null || adminSenha.isBlank()) {
            throw new IllegalStateException("ADMIN_SENHA não definida. Configure a variável de ambiente antes de criar o admin.");
        }
        if (adminSenha.length() < TAMANHO_MINIMO_SENHA) {
            throw new IllegalStateException("ADMIN_SENHA deve ter pelo menos " + TAMANHO_MINIMO_SENHA + " caracteres.");
        }
        if (SENHAS_PROIBIDAS.contains(adminSenha.toLowerCase())) {
            throw new IllegalStateException("ADMIN_SENHA usa um valor padrão conhecido. Escolha outra senha.");
        }
    }
}
