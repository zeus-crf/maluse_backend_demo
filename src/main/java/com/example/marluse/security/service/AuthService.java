package com.example.marluse.security.service;

import com.example.marluse.security.dto.UsuarioResponse;
import com.example.marluse.security.dto.AuthRequest;
import com.example.marluse.security.dto.RegisterRequest;
import com.example.marluse.security.model.Usuario;
import com.example.marluse.security.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${security.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${security.jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${security.cookie.secure:true}")
    private boolean cookieSecure;

    public UsuarioResponse login(@Valid AuthRequest request, HttpServletResponse response) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        Usuario usuario = (Usuario) userDetailsService.loadUserByUsername(request.email());

        var accessToken = jwtService.generateAccessToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        setarCookies(response, accessToken, refreshToken);

        return new UsuarioResponse(usuario.getNome(), usuario.getEmail());
    }

    public UsuarioResponse register(@Valid RegisterRequest request, HttpServletResponse response) {

        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Esse email já está cadastrado, logue com ele");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome())
                .email(request.email())
                .senha(passwordEncoder.encode(request.password()))
                .ativo(true)
                .build();

        usuarioRepository.save(usuario);

        var accessToken = jwtService.generateAccessToken(usuario);
        var refreshToken = jwtService.generateRefreshToken(usuario);

        setarCookies(response, accessToken, refreshToken);

        return new UsuarioResponse(usuario.getNome(), usuario.getEmail());
    }

    public UsuarioResponse me(UserDetails userDetails) {

        var usuario = usuarioRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        return new UsuarioResponse(usuario.getNome(), usuario.getEmail());
    }

    public UsuarioResponse refresh(String refreshTokenValue, HttpServletResponse response) {

        var refreshToken = refreshTokenService.validar(refreshTokenValue);

        var usuario = refreshToken.getUsuario();
        refreshTokenService.revogar(refreshTokenValue);

        var novoAccess = jwtService.generateAccessToken(usuario);
        var novoRefresh = jwtService.generateRefreshToken(usuario);

        setarCookies(response, novoAccess, novoRefresh);

        return new UsuarioResponse(usuario.getNome(), usuario.getEmail());
    }

    public void logout(String refreshValue, HttpServletResponse response) {
        refreshTokenService.revogar(refreshValue);
        limparCookies(response);
    }

    private String sameSitePolicy() {
        // SameSite=None exige Secure=true (HTTPS). Em dev (secure=false) usamos Lax.
        return cookieSecure ? "None" : "Lax";
    }

    private void setarCookies(HttpServletResponse response, String accessToken, String refreshToken) {

        ResponseCookie access = ResponseCookie.from("access_token", accessToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(accessExpiration / 1000)
                .sameSite(sameSitePolicy())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, access.toString());

        ResponseCookie refresh = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .maxAge(refreshExpiration / 1000)
                .sameSite(sameSitePolicy())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    }

    private void limparCookies(HttpServletResponse response) {

        ResponseCookie access = ResponseCookie.from("access_token")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/")
                .maxAge(0)
                .sameSite(sameSitePolicy())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, access.toString());

        ResponseCookie refresh = ResponseCookie.from("refresh_token")
                .httpOnly(true)
                .secure(cookieSecure)
                .path("/api/auth")
                .maxAge(0)
                .sameSite(sameSitePolicy())
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, refresh.toString());
    }
}
