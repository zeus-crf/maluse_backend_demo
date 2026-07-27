package com.example.marluse.estoque.controller;

import com.example.marluse.estoque.dto.FornecedorResponse;
import com.example.marluse.estoque.service.FornecedorService;
import com.example.marluse.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fornecedores")
@RequiredArgsConstructor
public class FornecedorController {

    private final FornecedorService fornecedorService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FornecedorResponse>>> listarFornecedores(){
        return ResponseEntity.ok(ApiResponse.ok(fornecedorService.listar()));
    }
}
