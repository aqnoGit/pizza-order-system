package com.pizzaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class UsuariosMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_USUARIO =
            "insert into usuarios (id, nome, telefone, perfil, credenciais) values (?, ?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaUsuariosPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type, is_nullable from information_schema.columns "
                        + "where table_name = 'usuarios'");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder("id", "nome", "telefone", "perfil", "credenciais", "criado_em");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "nome")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "telefone")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "perfil")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "credenciais")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "criado_em")).isEqualTo("timestamp without time zone");
    }

    @Test
    void credenciaisEOpcional() {
        Object anulavel = jdbcTemplate.queryForObject(
                "select is_nullable from information_schema.columns "
                        + "where table_name = 'usuarios' and column_name = 'credenciais'",
                String.class);

        assertThat(anulavel).isEqualTo("YES");
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'usuarios' and column_name = 'id' and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void aceitaOsQuatroPerfisEPreencheCriadoEm() {
        UUID clienteId = UUID.randomUUID();
        jdbcTemplate.update(INSERIR_USUARIO, clienteId, "Ana", "11999990001", "CLIENTE", null);
        jdbcTemplate.update(INSERIR_USUARIO, UUID.randomUUID(), "Bia", "11999990002", "GARCOM", "hash");
        jdbcTemplate.update(INSERIR_USUARIO, UUID.randomUUID(), "Caio", "11999990003", "COZINHA", "hash");
        jdbcTemplate.update(INSERIR_USUARIO, UUID.randomUUID(), "Eva", "11999990005", "ADMIN", "hash");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from usuarios where perfil in ('CLIENTE', 'GARCOM', 'COZINHA', 'ADMIN')", Integer.class);
        Integer comCriadoEm = jdbcTemplate.queryForObject(
                "select count(*) from usuarios where id = ? and criado_em is not null", Integer.class, clienteId);

        assertThat(total).isGreaterThanOrEqualTo(4);
        assertThat(comCriadoEm).isEqualTo(1);
    }

    @Test
    void rejeitaPerfilInvalido() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_USUARIO, UUID.randomUUID(), "Dan", "11999990004", "SUPERUSUARIO", null))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Object tipoDa(List<Map<String, Object>> colunas, String nome) {
        return colunas.stream()
                .filter(coluna -> coluna.get("column_name").equals(nome))
                .findFirst()
                .orElseThrow()
                .get("data_type");
    }
}
