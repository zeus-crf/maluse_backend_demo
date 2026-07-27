package com.example.marluse.financeiro.scheduler;

import com.example.marluse.financeiro.enums.StatusLancamento;
import com.example.marluse.financeiro.model.LancamentoFinanceiro;
import com.example.marluse.financeiro.repository.LancamentoFinanceiroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceiroScheduler {

    private final LancamentoFinanceiroRepository lancamentoRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void  gerarLancamentosRecorrentes() {
        List<LancamentoFinanceiro> ultimos = lancamentoRepository.findUltimosPorGrupoAtivo();

        for (LancamentoFinanceiro ultimo : ultimos ) {
            // Cria o próximo quando o atual vencer (dataVencimento <= hoje)
            if (!ultimo.getDataVencimento().isAfter(LocalDate.now())) {
                LocalDate proximaData = calcularProximaData(ultimo);

                boolean jaExiste = lancamentoRepository
                        .findByRecorrenciaGrupoId(ultimo.getRecorrenciaGrupoId())
                        .stream()
                        .anyMatch(l -> l.getDataVencimento().equals(proximaData));

                if (!jaExiste) {
                    LancamentoFinanceiro novo = LancamentoFinanceiro.builder()
                            .tipo(ultimo.getTipo())
                            .categoria(ultimo.getCategoria())
                            .descricao(ultimo.getDescricao())
                            .valor(ultimo.getValor())
                            .status(StatusLancamento.PENDENTE)
                            .dataVencimento(proximaData)
                            .recorrencia(ultimo.getRecorrencia())
                            .recorrenciaGrupoId(ultimo.getRecorrenciaGrupoId())
                            .recorrenciaAtiva(true)
                            .cliente(ultimo.getCliente())
                            .build();

                    lancamentoRepository.save(novo);
                    log.info("Lançamento recorrente gerado: grupo={} data={}",
                            ultimo.getRecorrenciaGrupoId(), proximaData);
                }
            }
        }
    }

    private LocalDate calcularProximaData(LancamentoFinanceiro ultimo) {
        return switch (ultimo.getRecorrencia()) {
            case DIARIA  -> ultimo.getDataVencimento().plusDays(1);
            case SEMANAL -> ultimo.getDataVencimento().plusWeeks(1);
            case MENSAL  -> ultimo.getDataVencimento().plusMonths(1);
            case ANUAL   -> ultimo.getDataVencimento().plusYears(1);
        };
    }
}
