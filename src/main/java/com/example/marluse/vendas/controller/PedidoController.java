package com.example.marluse.vendas.controller;

import com.example.marluse.clientes.dto.ClienteResponse;
import com.example.marluse.clientes.service.ClienteService;
import com.example.marluse.estoque.dto.ProdutoResponse;
import com.example.marluse.estoque.service.ProdutoService;
import com.example.marluse.financeiro.dto.ParcelaResponse;
import com.example.marluse.shared.ApiResponse;
import com.example.marluse.vendas.dto.PedidoAtualizarRequest;
import com.example.marluse.vendas.dto.PedidoRequest;
import com.example.marluse.vendas.dto.PedidoResponse;
import com.example.marluse.vendas.enums.StatusPedido;
import com.example.marluse.vendas.model.Pedido;
import com.example.marluse.vendas.service.PedidoService;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;
    private final ClienteService clienteService;
    private final ProdutoService produtoService;

    @PostMapping
    public ResponseEntity<ApiResponse<PedidoResponse>> criar(@Valid @RequestBody PedidoRequest request){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Pedido criado com sucesso!", pedidoService.criar(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listar(){
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listar()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> listarPorId(@PathVariable String id){
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.buscarPorId(id)));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<List<PedidoResponse>>> listarPorStatus(@PathVariable StatusPedido status) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarPorStatus(status)));
    }

    @GetMapping("/clientes")
    public ResponseEntity<ApiResponse<List<ClienteResponse>>> listarClientes(){
        return ResponseEntity.ok(ApiResponse.ok(clienteService.listar()));
    }

    @GetMapping("/produtos")
    public ResponseEntity<ApiResponse<List<ProdutoResponse>>> listarProdutos(){
        return ResponseEntity.ok(ApiResponse.ok(produtoService.listar()));
    }

    @GetMapping("/somar-vendas")
    public ResponseEntity<ApiResponse<BigDecimal>> somarVendasPorPeriodo(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso  = DateTimeFormat.ISO.DATE) LocalDate fim){
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.somarVendasPorPeriodo(inicio, fim)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PedidoResponse>> atualizar(
            @PathVariable String id,
            @Valid @RequestBody PedidoAtualizarRequest request) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido atualizado", pedidoService.atualizar(id, request)));
    }

    @PatchMapping("/{id}/pagar")
    public ResponseEntity<ApiResponse<PedidoResponse>> pagar(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Pagamento registrado com sucesso", pedidoService.pagar(id)));
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<ApiResponse<PedidoResponse>> cancelar(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Pedido cancelado", pedidoService.cancelar(id)));
    }

    @PatchMapping("/{id}/confirmar")
    public ResponseEntity<ApiResponse<PedidoResponse>> confirmar(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok("Orçamento confirmado como venda", pedidoService.confirmar(id)));
    }

    @GetMapping("/{id}/parcelas")
    public ResponseEntity<ApiResponse<List<ParcelaResponse>>> listarParcelas(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.ok(pedidoService.listarParcelas(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable String id) {
        pedidoService.excluir(id);
        return ResponseEntity.noContent().build();
    }

}
