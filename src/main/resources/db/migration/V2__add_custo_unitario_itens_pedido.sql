-- V2 — Adiciona custo_unitario em itens_pedido (PR #19 produtos-lucro)
ALTER TABLE itens_pedido
    ADD COLUMN custo_unitario DECIMAL(10,2) NULL;
