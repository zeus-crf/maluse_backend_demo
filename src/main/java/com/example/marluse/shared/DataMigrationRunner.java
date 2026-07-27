package com.example.marluse.shared;

import com.example.marluse.vendas.repository.ItemPedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataMigrationRunner implements ApplicationRunner {

    private final ItemPedidoRepository itemPedidoRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int atualizados = itemPedidoRepository.backfillCustoUnitario();
        if (atualizados > 0) {
            log.info("Backfill: {} item(ns) de pedido preenchidos com custoUnitario do produto atual.", atualizados);
        }
    }
}
