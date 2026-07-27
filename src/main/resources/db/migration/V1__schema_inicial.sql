-- ============================================================
-- V1 — Schema inicial Marluse
-- Gerado a partir das entidades Hibernate
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id         VARCHAR(36)  NOT NULL,
    nome       VARCHAR(255) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    senha      VARCHAR(255) NOT NULL,
    ativo      TINYINT(1)   NOT NULL DEFAULT 1,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_usuarios_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         VARCHAR(36)  NOT NULL,
    token      VARCHAR(512) NOT NULL,
    usuario_id VARCHAR(36)  NOT NULL,
    expires_at DATETIME     NOT NULL,
    revogado   TINYINT(1)   NOT NULL DEFAULT 0,
    created_at DATETIME,
    updated_at DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_refresh_tokens_token (token),
    CONSTRAINT fk_refresh_tokens_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS clientes (
    id               VARCHAR(36)  NOT NULL,
    nome             VARCHAR(255) NOT NULL,
    cpf_cnpj         VARCHAR(18),
    telefone         VARCHAR(20),
    email            VARCHAR(255),
    endereco         VARCHAR(512),
    consumidor_final TINYINT(1)   NOT NULL DEFAULT 0,
    ativo            TINYINT(1)   NOT NULL DEFAULT 1,
    created_at       DATETIME,
    updated_at       DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_clientes_cpf_cnpj (cpf_cnpj)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS produtos (
    id                 VARCHAR(36)   NOT NULL,
    nome               VARCHAR(255)  NOT NULL,
    descricao          VARCHAR(512),
    preco              DECIMAL(10,2) NOT NULL,
    quantidade_estoque INT           NOT NULL DEFAULT 0,
    estoque_minimo     INT                    DEFAULT 0,
    ativo              TINYINT(1)    NOT NULL DEFAULT 1,
    medida             VARCHAR(50)   NOT NULL,
    valor_compra       DECIMAL(10,2),
    created_at         DATETIME,
    updated_at         DATETIME,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS pedidos (
    id                   VARCHAR(36)   NOT NULL,
    numero               BIGINT        NOT NULL,
    cliente_id           VARCHAR(36),
    status               VARCHAR(20)   NOT NULL,
    forma_pagamento      VARCHAR(20),
    valor_total          DECIMAL(10,2) NOT NULL,
    observacao           VARCHAR(1000),
    data_vencimento      DATE,
    desconto             DECIMAL(10,2),
    tipo_desconto        VARCHAR(10),
    desconto_aplicado_em DATE,
    juros                DECIMAL(10,2),
    tipo_juros           VARCHAR(10),
    juros_aplicado_em    DATE,
    data_movimento       DATE,
    created_at           DATETIME,
    updated_at           DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS itens_pedido (
    id             VARCHAR(36)   NOT NULL,
    pedido_id      VARCHAR(36)   NOT NULL,
    produto_id     VARCHAR(36)   NOT NULL,
    quantidade     INT           NOT NULL,
    preco_unitario DECIMAL(10,2) NOT NULL,
    sub_total      DECIMAL(10,2) NOT NULL,
    created_at     DATETIME,
    updated_at     DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_itens_pedido_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedidos  (id),
    CONSTRAINT fk_itens_pedido_produto FOREIGN KEY (produto_id) REFERENCES produtos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS locacoes (
    id                      VARCHAR(36)   NOT NULL,
    numero                  BIGINT        NOT NULL,
    cliente_id              VARCHAR(36),
    status                  VARCHAR(20)   NOT NULL,
    forma_pagamento         VARCHAR(20),
    data_retirada           DATE          NOT NULL,
    data_devolucao_prevista DATE          NOT NULL,
    data_devolucao_real     DATE,
    valor_total             DECIMAL(10,2) NOT NULL,
    observacao              VARCHAR(1000),
    desconto                DECIMAL(10,2),
    tipo_desconto           VARCHAR(10),
    desconto_aplicado_em    DATE,
    juros                   DECIMAL(10,2),
    tipo_juros              VARCHAR(10),
    juros_aplicado_em       DATE,
    data_movimento          DATE,
    created_at              DATETIME,
    updated_at              DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_locacoes_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS itens_locacao (
    id           VARCHAR(36)   NOT NULL,
    locacao_id   VARCHAR(36)   NOT NULL,
    produto_id   VARCHAR(36)   NOT NULL,
    quantidade   INT           NOT NULL,
    preco_diaria DECIMAL(10,2) NOT NULL,
    subtotal     DECIMAL(10,2) NOT NULL,
    created_at   DATETIME,
    updated_at   DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_itens_locacao_locacao  FOREIGN KEY (locacao_id) REFERENCES locacoes (id),
    CONSTRAINT fk_itens_locacao_produto  FOREIGN KEY (produto_id) REFERENCES produtos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS entregas (
    id            VARCHAR(36) NOT NULL,
    pedido_id     VARCHAR(36),
    locacao_id    VARCHAR(36),
    endereco      VARCHAR(512),
    data_prevista DATE,
    data_entrega  DATE,
    status        VARCHAR(30),
    created_at    DATETIME,
    updated_at    DATETIME,
    PRIMARY KEY (id),
    UNIQUE KEY uk_entregas_pedido  (pedido_id),
    UNIQUE KEY uk_entregas_locacao (locacao_id),
    CONSTRAINT fk_entregas_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedidos  (id),
    CONSTRAINT fk_entregas_locacao FOREIGN KEY (locacao_id) REFERENCES locacoes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ----------------------------------------------------------------

CREATE TABLE IF NOT EXISTS lancamento_financeiros (
    id                   VARCHAR(36)   NOT NULL,
    tipo                 VARCHAR(20)   NOT NULL,
    categoria            VARCHAR(100)  NOT NULL,
    descricao            VARCHAR(512)  NOT NULL,
    valor                DECIMAL(10,2) NOT NULL,
    data_vencimento      DATE,
    data_pagamento       DATE,
    status               VARCHAR(20)   NOT NULL DEFAULT 'PENDENTE',
    recorrencia          VARCHAR(20),
    recorrencia_grupo_id VARCHAR(36),
    recorrencia_ativa    TINYINT(1)             DEFAULT 1,
    cliente_id           VARCHAR(36),
    pedido_id            VARCHAR(36),
    locacao_id           VARCHAR(36),
    num_parcelas         INT,
    total_parcelas       INT,
    created_at           DATETIME,
    updated_at           DATETIME,
    PRIMARY KEY (id),
    CONSTRAINT fk_lancamentos_cliente FOREIGN KEY (cliente_id) REFERENCES clientes (id),
    CONSTRAINT fk_lancamentos_pedido  FOREIGN KEY (pedido_id)  REFERENCES pedidos  (id),
    CONSTRAINT fk_lancamentos_locacao FOREIGN KEY (locacao_id) REFERENCES locacoes (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
