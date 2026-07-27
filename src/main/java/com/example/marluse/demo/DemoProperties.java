package com.example.marluse.demo;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.demo")
public class DemoProperties {
    private boolean enabled = false;
    private long resetIntervalMs = 21_600_000L; // 6h
    private String usuarioEmail = "demo@marluse.com";
    private String usuarioSenha = "demo123";
    private String usuarioNome = "Visitante Demo";
    private int clientes = 18;
    private int fornecedores = 6;
    private int produtos = 28;
    private int pedidos = 45;
    private int locacoes = 22;
    private int lancamentos = 30;
}
