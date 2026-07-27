package com.example.marluse.estoque.controller;

import com.example.marluse.estoque.dto.ProdutoAtualizarRequest;
import com.example.marluse.estoque.dto.ProdutoRequest;
import com.example.marluse.estoque.dto.ProdutoResponse;
import com.example.marluse.estoque.service.ProdutoService;
import com.example.marluse.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/produtos")
@RequiredArgsConstructor
public class ProdutoController {

    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ProdutoResponse>> criar(@Valid @RequestBody ProdutoRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Produto criado com sucesso", produtoService.criar(request)));
    }

    @GetMapping
    public  ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarAtivos(){
        return ResponseEntity.ok(ApiResponse.ok(produtoService.listar()));
    }

    @GetMapping("/rascunhos")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarRascunhos() {
        return ResponseEntity.ok(ApiResponse.ok(produtoService.listarRascunho()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoResponse>> listarPorId(@PathVariable String id){
        return ResponseEntity.ok(ApiResponse.ok(produtoService.burcarPorId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProdutoResponse>> atualizar(@PathVariable String id, @Valid @RequestBody ProdutoAtualizarRequest request){
        return ResponseEntity.ok(ApiResponse.ok("Produto atualizado com sucesso !", produtoService.atualizar(id, request)));
    }

    @GetMapping("/estoque-baixo")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarEstoqueBaixo() {
        return ResponseEntity.ok(ApiResponse.ok(produtoService.listarEstoqueBaixo()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable String id){
        produtoService.inativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Produto inativado com sucesso!", null));
    }
}
