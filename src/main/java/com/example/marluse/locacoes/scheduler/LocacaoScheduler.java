package com.example.marluse.locacoes.scheduler;

import com.example.marluse.locacoes.enums.StatusLocacao;
import com.example.marluse.locacoes.repository.LocacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocacaoScheduler {

    private final LocacaoRepository locacaoRepository;

    /** Roda na inicialização para já corrigir locações vencidas sem esperar a meia-noite. */
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void marcarAtrasadasNaInicializacao() {
        marcarLocacoesAtrasadas();
    }

    /**
     * Executa todo dia à meia-noite.
     * Marca como ATRASADA toda locação ATIVA cuja dataDevolucaoPrevista já passou.
     * Um único UPDATE no banco — sem carregar objetos na memória.
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void marcarLocacoesAtrasadas() {
        int atualizadas = locacaoRepository.marcarAtrasadas(
                StatusLocacao.ATIVA, StatusLocacao.ATRASADA, LocalDate.now());

        if (atualizadas > 0)
            log.info("[LocacaoScheduler] {} locação(ões) marcada(s) como ATRASADA.", atualizadas);
    }
}
