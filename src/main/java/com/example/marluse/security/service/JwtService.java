package com.example.marluse.security.service;

import com.example.marluse.security.model.RefreshToken;
import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.RefreshTokenRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JwtService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${security.jwt.secret}")
    private String secretKey;

    @Value("${security.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;

    // HS256 exige no mínimo 256 bits (32 bytes) de chave
    private static final int TAMANHO_MINIMO_SECRET = 32;

    // SHA-256 da chave de exemplo que circula em tutoriais e já foi commitada neste repo.
    // Comparamos pelo hash para não recolocar a chave no código.
    private static final String HASH_SECRET_VAZADO =
            "3354cfcf65c7b2e6840ea6f880aede239750cb1d063950c4e8fd9fbf9ec73a32";

    @PostConstruct
    void validarSecret() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT_SECRET não definido. Configure a variável de ambiente.");
        }
        if (secretKey.getBytes(StandardCharsets.UTF_8).length < TAMANHO_MINIMO_SECRET) {
            throw new IllegalStateException(
                    "JWT_SECRET muito curto: use pelo menos " + TAMANHO_MINIMO_SECRET + " bytes (ex: openssl rand -base64 64).");
        }
        if (HASH_SECRET_VAZADO.equals(sha256(secretKey))) {
            throw new IllegalStateException(
                    "JWT_SECRET é a chave pública de exemplo que vazou no histórico do repo. Gere uma nova.");
        }
    }

    private static String sha256(String valor) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(valor.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    public String generateAccessToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + accessExpiration))
                .signWith(getSignKey())
                .compact();
    }

    @Transactional
    public String generateRefreshToken(Usuario usuario) {

        List<RefreshToken> listRefresh = refreshTokenRepository.findAllByUsuario(usuario);

        listRefresh.forEach(l -> l.setRevogado(true));

        refreshTokenRepository.saveAll(listRefresh);

        var refreshToken = UUID.randomUUID().toString();

        RefreshToken refresh = RefreshToken.builder()
                .token(refreshToken)
                .usuario(usuario)
                .expiresAt(LocalDateTime.now().plus(refreshExpiration, ChronoUnit.MILLIS))
                .revogado(false)
                .build();

        refreshTokenRepository.save(refresh);

        return refreshToken;
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(Jwts.parser().verifyWith(getSignKey()).build()
                .parseSignedClaims(token).getPayload());
    }

    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }
}