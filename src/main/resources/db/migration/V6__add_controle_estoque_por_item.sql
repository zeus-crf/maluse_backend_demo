ALTER TABLE itens_pedido
    ADD COLUMN baixar_estoque        TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN permitir_sem_estoque  TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN estoque_descontado    TINYINT(1) NOT NULL DEFAULT 0;

ALTER TABLE itens_locacao
    ADD COLUMN baixar_estoque        TINYINT(1) NOT NULL DEFAULT 1,
    ADD COLUMN permitir_sem_estoque  TINYINT(1) NOT NULL DEFAULT 0,
    ADD COLUMN estoque_descontado    TINYINT(1) NOT NULL DEFAULT 0;

UPDATE itens_pedido i
    JOIN pedidos p ON p.id = i.pedido_id
    SET i.estoque_descontado = p.estoque_descontado;

UPDATE itens_locacao i
    JOIN locacoes l ON l.id = i.locacao_id
    SET i.estoque_descontado = l.estoque_descontado;