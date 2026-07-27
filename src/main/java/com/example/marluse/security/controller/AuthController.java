package com.example.marluse.security.controller;

import com.example.marluse.security.dto.AuthRequest;
import com.example.marluse.security.dto.RegisterRequest;
import com.example.marluse.security.dto.UsuarioResponse;
import com.example.marluse.security.service.AuthService;
import com.example.marluse.shared.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UsuarioResponse>> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response){
        return ResponseEntity.ok(ApiResponse.ok("Login realizado com sucesso", authService.login(request, response)));
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UsuarioResponse>> register(@Valid @RequestBody RegisterRequest request, HttpServletResponse response){
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Usuário criado com sucesso", authService.register(request, response)));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UsuarioResponse>> refresh (@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response) {

        if (refreshToken == null){
            throw new IllegalArgumentException("O refresh token ausente");
        }

        return ResponseEntity.ok(ApiResponse.ok(authService.refresh(refreshToken, response)));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@CookieValue(name = "refresh_token", required = false) String refreshToken, HttpServletResponse response ){
        authService.logout(refreshToken, response); // limparCookies() é chamado sempre; revogar(null) é silencioso
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioResponse>> me (@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.ok(authService.me(userDetails)));
    }

}
