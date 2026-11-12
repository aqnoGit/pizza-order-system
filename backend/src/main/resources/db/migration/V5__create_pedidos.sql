CREATE TABLE pedidos (
    id UUID PRIMARY KEY,
    comanda_id UUID,
    cliente_id UUID,
    origem VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL,
    registrado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inicio_preparo TIMESTAMP,
    finalizado_em TIMESTAMP,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_pedidos_comanda FOREIGN KEY (comanda_id) REFERENCES comandas (id),
    CONSTRAINT fk_pedidos_cliente FOREIGN KEY (cliente_id) REFERENCES usuarios (id),
    CONSTRAINT ck_pedidos_origem CHECK (origem IN ('PRESENCIAL', 'ENTREGA')),
    CONSTRAINT ck_pedidos_status CHECK (status IN ('REGISTRADO', 'EM_PREPARO', 'PRONTO', 'ENTREGUE'))
);
