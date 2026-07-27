package com.example.marluse.clientes.controller;

import com.example.marluse.clientes.dto.*;
import com.example.marluse.clientes.service.ClienteService;
import com.example.marluse.clientes.service.ObservacaoClienteService;
import com.example.marluse.financeiro.dto.AbatimentoRequest;
import com.example.marluse.financeiro.dto.AbatimentoResultado;
import com.example.marluse.financeiro.dto.AbatimentoResumo;
import com.example.marluse.financeiro.service.AbatimentoService;
import com.example.marluse.shared.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.SimpleTimeZone;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;
    private final AbatimentoService abatimentoService;
    private final ObservacaoClienteService observacaoService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> criar(@Valid @RequestBody ClienteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Cliente criado com sucesso!", clienteService.criar(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> listarPorId(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listarPorId(id)));
    }

    @GetMapping("/{id}/historico")
    public ResponseEntity<ApiResponse<ClienteHistoricoResponse>> historicoCliente(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.historicoCliente(id)));
    }

    @GetMapping("/{id}/saldo")
    public ResponseEntity<ApiResponse<ClienteSaldoResponse>> saldo(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(clienteService.saldoCliente(id)));
    }

    @PostMapping("/{id}/abatimentos")
    public ResponseEntity<ApiResponse<AbatimentoResultado>> debitar(@PathVariable String id, @Valid @RequestBody AbatimentoRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Abatimento feito com sucesso", abatimentoService.debitar(id, request)));
    }

    @GetMapping("/{id}/abatimentos")
    public ResponseEntity<ApiResponse<List<AbatimentoResumo>>> listarAbatimentos(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(abatimentoService.listarAbatimentos(id)));
    }

    @PostMapping("/abatimentos/{abatimentoId}/estorno")
    public ResponseEntity<ApiResponse<Void>> estornar(@PathVariable String abatimentoId) {
        abatimentoService.estornar(abatimentoId);
        return ResponseEntity.ok(ApiResponse.ok("Abatimento estornado com sucesso", null));
    }

    @PostMapping("/{id}/observacoes")
    public ResponseEntity<ApiResponse<ObservacaoResponse>> criarObservacao(
            @PathVariable String id,
            @Valid @RequestBody ObservacaoRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ObservacaoResponse response = observacaoService.criar(id, request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Observação registrada com sucesso", response));
    }

    @GetMapping("/{id}/observacoes")
    public ResponseEntity<ApiResponse<List<ObservacaoResponse>>> listarObservacoes(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(observacaoService.listar(id)));
    }

    @DeleteMapping("/observacoes/{observacaoId}")
    public ResponseEntity<ApiResponse<Void>> deletarObservacao(@PathVariable String observacaoId) {
        observacaoService.deletar(observacaoId);
        return ResponseEntity.ok(ApiResponse.ok("Observação removida com sucesso", null));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> atualizar(
            @PathVariable String id,
            @Valid @RequestBody ClienteAtualizarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Cliente atualizado com sucesso", clienteService.atualizar(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> inativar(@PathVariable String id){
        clienteService.inativar(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente inativado com sucesso", null));
    }


}
