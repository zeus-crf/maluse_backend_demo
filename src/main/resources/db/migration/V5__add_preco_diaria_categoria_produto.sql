-- Idempotente: seguro em bancos onde a V5 aplicou parcialmente (prod)
-- e em bancos limpos (dev/staging).

-- preco_diaria: cria só se não existir (MySQL não tem ADD COLUMN IF NOT EXISTS)
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'produtos' AND COLUMN_NAME = 'preco_diaria');
SET @ddl := IF(@exists = 0,
    'ALTER TABLE produtos ADD COLUMN preco_diaria DECIMAL(10,2) NULL', 'DO 0');
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

-- categoria: cria só se não existir
SET @exists := (SELECT COUNT(*) FROM information_schema.COLUMNS
                WHERE TABLE_SCHEMA = DATABASE()
                  AND TABLE_NAME = 'produtos' AND COLUMN_NAME = 'categoria');
SET @ddl := IF(@exists = 0,
    'ALTER TABLE produtos ADD COLUMN categoria VARCHAR(30) NULL', 'DO 0');
PREPARE s FROM @ddl; EXECUTE s; DEALLOCATE PREPARE s;

-- backfill (idempotente: só afeta NULLs)
UPDATE produtos SET preco_diaria = preco WHERE preco_diaria IS NULL;
UPDATE produtos SET categoria = 'OUTROS' WHERE categoria IS NULL;

-- NOT NULL (idempotente: reaplica o mesmo tipo)
ALTER TABLE produtos
    MODIFY COLUMN preco_diaria DECIMAL(10,2) NOT NULL,
    MODIFY COLUMN categoria    VARCHAR(30)   NOT NULL;
