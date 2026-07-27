package com.example.marluse.relatorios.controller;

import com.example.marluse.relatorios.dto.*;
import com.example.marluse.relatorios.service.RelatoriosService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/relatorios")
@RequiredArgsConstructor
public class RelatoriosController {

    private final RelatoriosService relatoriosService;

    @GetMapping("/kpis")
    public ResponseEntity<KpisResponse> kpis(
            @RequestParam(defaultValue = "6m") String periodo) {
        return ResponseEntity.ok(relatoriosService.kpis(periodo));
    }

    @GetMapping("/receita-mensal")
    public ResponseEntity<List<ReceitaMensalItemResponse>> receitaMensal(
            @RequestParam(defaultValue = "6") int meses) {
        return ResponseEntity.ok(relatoriosService.receitaMensal(meses));
    }

    @GetMapping("/status-financeiro")
    public ResponseEntity<StatusFinanceiroResponse> statusFinanceiro() {
        return ResponseEntity.ok(relatoriosService.statusFinanceiro());
    }

    @GetMapping("/top-clientes")
    public ResponseEntity<List<TopClienteResponse>> topClientes(
            @RequestParam(defaultValue = "5") int limite,
            @RequestParam(defaultValue = "6m") String periodo) {
        return ResponseEntity.ok(relatoriosService.topClientes(limite, periodo));
    }

    @GetMapping("/top-produtos")
    public ResponseEntity<List<TopProdutoResponse>> topProdutos(
            @RequestParam(defaultValue = "5") int limite,
            @RequestParam(defaultValue = "6m") String periodo) {
        return ResponseEntity.ok(relatoriosService.topProdutos(limite, periodo));
    }
}
