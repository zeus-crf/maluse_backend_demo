package com.example.marluse.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public record AuthRequest(
        @NotBlank(message = "O email é obrigatório") String email,
        @NotBlank(message = "A senha é obrigatória") String password
) {
}
