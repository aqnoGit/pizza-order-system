CREATE TABLE mesas (
    id UUID PRIMARY KEY,
    numero INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT ck_mesas_status CHECK (status IN ('LIVRE', 'OCUPADA'))
);
