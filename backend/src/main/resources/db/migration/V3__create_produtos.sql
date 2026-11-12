CREATE TABLE produtos (
    id UUID PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao VARCHAR(255),
    preco NUMERIC(10, 2) NOT NULL,
    categoria VARCHAR(50) NOT NULL,
    disponivel BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT ck_produtos_preco CHECK (preco >= 0)
);
