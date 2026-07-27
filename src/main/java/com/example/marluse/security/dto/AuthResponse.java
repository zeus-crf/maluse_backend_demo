package com.example.marluse.security.dto;

import lombok.Builder;

@Builder
public record AuthResponse (
        String email,
        String nome
){
}
