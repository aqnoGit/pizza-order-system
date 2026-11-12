ALTER TABLE usuarios DROP CONSTRAINT ck_usuarios_perfil;
ALTER TABLE usuarios ADD CONSTRAINT ck_usuarios_perfil CHECK (perfil IN ('CLIENTE', 'GARCOM', 'COZINHA', 'ADMIN'));
