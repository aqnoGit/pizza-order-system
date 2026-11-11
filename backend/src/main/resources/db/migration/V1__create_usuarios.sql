CREATE TABLE usuarios (
    id UUID PRIMARY KEY,
    nome VARCHAR(120) NOT NULL,
    telefone VARCHAR(20) NOT NULL,
    perfil VARCHAR(20) NOT NULL,
    credenciais VARCHAR(255),
    criado_em TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('CLIENTE', 'GARCOM', 'COZINHA'))
);
