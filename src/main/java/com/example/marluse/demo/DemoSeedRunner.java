package com.example.marluse.demo;

import com.example.marluse.clientes.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("demo")
@RequiredArgsConstructor
public class DemoSeedRunner {

    private final DemoDataService demoDataService;
    private final ClienteRepository clienteRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void seedOnStartup() {
        demoDataService.ensureDemoUser();
        if (clienteRepository.count() == 0) {
            log.info("[Demo] Banco vazio — executando seed inicial.");
            demoDataService.seed();
        } else {
            log.info("[Demo] Dados já presentes — seed inicial ignorada.");
        }
    }
}
