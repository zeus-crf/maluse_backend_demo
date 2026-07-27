-- Abatimento de dívida do cliente: pagamento parcial de lançamentos (valor_pago)
-- + registro auditável de cada débito (abatimentos / abatimento_parcela).

ALTER TABLE lancamento_financeiros
    ADD COLUMN valor_pago DECIMAL(10,2) NOT NULL DEFAULT 0;

-- Um abatimento = um débito registrado pelo operador (o valor total digitado).
CREATE TABLE abatimentos (
    id            VARCHAR(36)  NOT NULL,
    cliente_id    VARCHAR(36)  NOT NULL,
    valor         DECIMAL(10,2) NOT NULL,
    data          DATE          NOT NULL,
    observacao    VARCHAR(255)  NULL,
    estornado     BOOLEAN       NOT NULL DEFAULT FALSE,
    estornado_em  DATE          NULL,
    created_at    DATETIME      NULL,
    updated_at    DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_abatimento_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
);

-- Distribuição do abatimento pelas parcelas (SUM(valor) = abatimentos.valor).
CREATE TABLE abatimento_parcela (
    id             VARCHAR(36)  NOT NULL,
    abatimento_id  VARCHAR(36)  NOT NULL,
    lancamento_id  VARCHAR(36)  NOT NULL,
    valor          DECIMAL(10,2) NOT NULL,
    created_at     DATETIME      NULL,
    updated_at     DATETIME      NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_abatparcela_abatimento FOREIGN KEY (abatimento_id) REFERENCES abatimentos (id),
    CONSTRAINT fk_abatparcela_lancamento FOREIGN KEY (lancamento_id) REFERENCES lancamento_financeiros (id)
);
