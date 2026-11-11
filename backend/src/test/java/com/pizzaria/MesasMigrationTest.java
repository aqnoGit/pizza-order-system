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

class MesasMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_MESA = "insert into mesas (id, numero, status) values (?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaMesasPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type, is_nullable from information_schema.columns "
                        + "where table_name = 'mesas' order by column_name");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder("id", "numero", "status");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "numero")).isEqualTo("integer");
        assertThat(tipoDa(colunas, "status")).isEqualTo("character varying");
        assertThat(colunas).allMatch(coluna -> coluna.get("is_nullable").equals("NO"));
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'mesas' and column_name = 'id' and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void aceitaStatusLivreEOcupada() {
        jdbcTemplate.update(INSERIR_MESA, UUID.randomUUID(), 1, "LIVRE");
        jdbcTemplate.update(INSERIR_MESA, UUID.randomUUID(), 2, "OCUPADA");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from mesas where status in ('LIVRE', 'OCUPADA')", Integer.class);

        assertThat(total).isGreaterThanOrEqualTo(2);
    }

    @Test
    void rejeitaStatusInvalido() {
        assertThatThrownBy(() -> jdbcTemplate.update(INSERIR_MESA, UUID.randomUUID(), 3, "QUEBRADA"))
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
