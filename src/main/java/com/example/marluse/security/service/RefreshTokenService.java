package com.example.marluse.security.service;

import com.example.marluse.security.model.RefreshToken;
import com.example.marluse.security.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;


    public RefreshToken validar(String token) {

        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token não encontrado"));

        if (refreshToken.isRevogado()) {
            throw new IllegalArgumentException("Token revogado");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado");
        }

        return refreshToken;
    }

    @Transactional
    public void revogar(String token) {

        refreshTokenRepository.findByToken(token).ifPresent(rt -> {
            rt.setRevogado(true);
            refreshTokenRepository.save(rt);
        });

    }
}
