package com.example.marluse.entrega.controller;

import com.example.marluse.entrega.dto.EntregaAtualizarRequest;
import com.example.marluse.entrega.dto.EntregaResponse;
import com.example.marluse.entrega.service.EntregaService;
import com.example.marluse.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/entregas")
@RequiredArgsConstructor
public class EntregaController {

    private final EntregaService entregaService;


    @PatchMapping("/{id}/entregar")
    public ResponseEntity<ApiResponse<EntregaResponse>> entregar(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Entrega marcada com feita com sucesso!", entregaService.entregar(id)));
    }

    @PutMapping("/{id}")
    ResponseEntity<ApiResponse<EntregaResponse>> editar(@PathVariable String id, @Valid @RequestBody EntregaAtualizarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Entrega editada com sucesso", entregaService.editar(id, request)));
    }
}
