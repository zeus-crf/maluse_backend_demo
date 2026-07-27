package com.example.marluse.dashboard.controller;

import com.example.marluse.dashboard.dto.DashboardKpisResponse;
import com.example.marluse.dashboard.dto.EstoqueCriticoResponse;
import com.example.marluse.dashboard.dto.GraficoItemResponse;
import com.example.marluse.dashboard.dto.LocacaoEmCursoResponse;
import com.example.marluse.dashboard.service.DashboardService;
import com.example.marluse.shared.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController {


    private final DashboardService dashboardService;

    @GetMapping("/kpis")
    public ResponseEntity<ApiResponse<DashboardKpisResponse>> getKpis(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim){
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getKpis(inicio, fim)));
    }

    @GetMapping("/grafico")
    public ResponseEntity<ApiResponse<List<GraficoItemResponse>>> getGrafico(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getGrafico(inicio, fim)));
    }

    @GetMapping("/estoque-critico")
    public ResponseEntity<ApiResponse<List<EstoqueCriticoResponse>>> getEstoqueCritico() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getEstoqueCritico()));
    }

    @GetMapping("/locacoes-em-curso")
    public ResponseEntity<ApiResponse<List<LocacaoEmCursoResponse>>> getLocacoesEmCurso() {
        return ResponseEntity.ok(ApiResponse.ok(dashboardService.getLocacoesEmCurso()));
    }

}
