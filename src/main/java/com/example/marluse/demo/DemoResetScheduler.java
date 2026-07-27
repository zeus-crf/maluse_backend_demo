package com.example.marluse.demo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoResetScheduler {

    private final DemoDataService demoDataService;

    /**
     * Limpa e re-semeia o banco periodicamente para o demo voltar ao estado ideal.
     * initialDelay = intervalo → não reseta logo após o startup (o DemoSeedRunner já semeou).
     */
    @Scheduled(fixedDelayString = "${app.demo.reset-interval-ms}",
               initialDelayString = "${app.demo.reset-interval-ms}")
    public void resetPeriodico() {
        log.info("[Demo] Reset periódico iniciado.");
        demoDataService.ensureDemoUser();
        demoDataService.clear();
        demoDataService.seed();
        log.info("[Demo] Reset periódico concluído.");
    }
}
