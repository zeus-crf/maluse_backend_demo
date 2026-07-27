package com.example.marluse.financeiro.controller;

import com.example.marluse.financeiro.dto.LancamentoAtualizarRequest;
import com.example.marluse.financeiro.dto.LancamentoFinanceiroRequest;
import com.example.marluse.financeiro.dto.LancamentoFinanceiroResponse;
import com.example.marluse.financeiro.service.LancamentoFinanceiroService;
import com.example.marluse.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/financeiro")
@RequiredArgsConstructor
public class LancamentoFinanceiroController {

    private final LancamentoFinanceiroService lancamentoService;

    @PostMapping
    public ResponseEntity<ApiResponse<LancamentoFinanceiroResponse>> criar(
            @Valid @RequestBody LancamentoFinanceiroRequest request) {
        LancamentoFinanceiroResponse response = lancamentoService.criar(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Lançamento criado com sucesso", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LancamentoFinanceiroResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.listarTodos()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LancamentoFinanceiroResponse>> buscarPorId(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.listarPorId(id)));
    }

    @GetMapping("/pendentes")
    public ResponseEntity<ApiResponse<List<LancamentoFinanceiroResponse>>> listarPendentes() {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.listarPendentes()));
    }

    @GetMapping("/recorrencia/{grupoId}")
    public ResponseEntity<ApiResponse<List<LancamentoFinanceiroResponse>>> listarGrupoId(@PathVariable String grupoId) {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.listarLancamentosGrupoId(grupoId)));
    }

    @GetMapping("/vencidos")
    public ResponseEntity<ApiResponse<List<LancamentoFinanceiroResponse>>> listarVencidos() {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.listarVencidos()));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<LancamentoFinanceiroResponse>> atualizar(
            @PathVariable String id,
            @Valid @RequestBody LancamentoAtualizarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Lançamento atualizado", lancamentoService.atualizar(id, request)));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<LancamentoFinanceiroResponse>> pagar(@PathVariable String id) {
        LancamentoFinanceiroResponse response = lancamentoService.pagar(id);
        return ResponseEntity.ok(ApiResponse.ok("Lançamento pago com sucesso", response));
    }

    @GetMapping("/resumo-dia")
    public ResponseEntity<ApiResponse<Object>> resumoDia() {
        return ResponseEntity.ok(ApiResponse.ok(lancamentoService.resumoDia()));
    }

    @PatchMapping("/recorrencia/{grupoId}/cancelar")
    public ResponseEntity<ApiResponse<Void>> cancelarRecorrencia(@PathVariable String grupoId) {
        lancamentoService.cancelarRecorrencia(grupoId);
        return ResponseEntity.ok(ApiResponse.ok("Recorrência cancelada", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletar(@PathVariable String id) {
        lancamentoService.deletar(id);
        return ResponseEntity.ok(ApiResponse.ok("Lançamento removido com sucesso", null));
    }
}