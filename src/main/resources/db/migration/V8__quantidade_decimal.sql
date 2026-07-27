-- Permite quantidades fracionadas (ex.: 1.5 m, 2.5 kg) para produtos vendidos
-- por medida contínua (METRO, METRO_QUADRADO, LITRO, KG, BALDE).
-- A restrição de quais unidades aceitam decimais é aplicada na interface;
-- no banco todas as quantidades passam a ser DECIMAL para suportar o caso.

ALTER TABLE produtos
    MODIFY COLUMN quantidade_estoque DECIMAL(12,3) NOT NULL DEFAULT 0;

ALTER TABLE itens_pedido
    MODIFY COLUMN quantidade DECIMAL(12,3) NOT NULL;

ALTER TABLE itens_locacao
    MODIFY COLUMN quantidade DECIMAL(12,3) NOT NULL;
