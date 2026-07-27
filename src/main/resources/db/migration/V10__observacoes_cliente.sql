-- Anotações livres sobre um cliente, escritas pelos usuários do sistema.
-- A data da anotação é o created_at — não há campo de data próprio.

CREATE TABLE observacoes_cliente (
    id          VARCHAR(36)   NOT NULL,
    cliente_id  VARCHAR(36)   NOT NULL,
    texto       VARCHAR(1000) NOT NULL,
    autor_id    VARCHAR(36)   NOT NULL,
    created_at  DATETIME      NULL,
    updated_at  DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_observacao_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_observacao_autor   FOREIGN KEY (autor_id)   REFERENCES usuarios (id)
);

-- A listagem é sempre por cliente, da mais recente para a mais antiga.
CREATE INDEX idx_observacao_cliente_data ON observacoes_cliente (cliente_id, created_at DESC);
