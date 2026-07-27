-- Adiciona flag de controle de baixa de estoque em pedidos e locações
-- Registros existentes: estoque já foi descontado na criação (comportamento antigo),
-- portanto iniciamos com TRUE para não descontar em duplicata.

ALTER TABLE pedidos
    ADD COLUMN estoque_descontado TINYINT(1) NOT NULL DEFAULT 1;

ALTER TABLE locacoes
    ADD COLUMN estoque_descontado TINYINT(1) NOT NULL DEFAULT 1;
