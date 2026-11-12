CREATE TABLE comandas (
    id UUID PRIMARY KEY,
    mesa_id UUID NOT NULL,
    garcom_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL,
    aberta_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fechada_em TIMESTAMP,
    valor_total NUMERIC(10, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_comandas_mesa FOREIGN KEY (mesa_id) REFERENCES mesas (id),
    CONSTRAINT fk_comandas_garcom FOREIGN KEY (garcom_id) REFERENCES usuarios (id),
    CONSTRAINT ck_comandas_status CHECK (status IN ('ABERTA', 'FECHADA'))
);
