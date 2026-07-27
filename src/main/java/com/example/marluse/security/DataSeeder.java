package com.example.marluse.security;

import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

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

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByEmail(adminEmail).isEmpty()) {
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
}
