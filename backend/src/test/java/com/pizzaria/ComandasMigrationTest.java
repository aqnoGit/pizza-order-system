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

class ComandasMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_COMANDA =
            "insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaComandasPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type, is_nullable from information_schema.columns "
                        + "where table_name = 'comandas'");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder(
                        "id", "mesa_id", "garcom_id", "status", "aberta_em", "fechada_em", "valor_total", "observacao");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "status")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "valor_total")).isEqualTo("numeric");
        assertThat(colunas)
                .filteredOn(coluna -> !List.of("fechada_em", "observacao").contains(coluna.get("column_name")))
                .allMatch(coluna -> coluna.get("is_nullable").equals("NO"));
        assertThat(colunas)
                .filteredOn(coluna -> List.of("fechada_em", "observacao").contains(coluna.get("column_name")))
                .allMatch(coluna -> coluna.get("is_nullable").equals("YES"));
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'comandas' and column_name = 'id' and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void aceitaStatusAbertaEFechada() {
        UUID mesaId = criarMesa();
        UUID garcomId = criarGarcom();

        jdbcTemplate.update(INSERIR_COMANDA, UUID.randomUUID(), mesaId, garcomId, "ABERTA");
        jdbcTemplate.update(INSERIR_COMANDA, UUID.randomUUID(), mesaId, garcomId, "FECHADA");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from comandas where mesa_id = ?", Integer.class, mesaId);
        assertThat(total).isEqualTo(2);
    }

    @Test
    void rejeitaStatusInvalido() {
        UUID mesaId = criarMesa();
        UUID garcomId = criarGarcom();

        assertThatThrownBy(() -> jdbcTemplate.update(INSERIR_COMANDA, UUID.randomUUID(), mesaId, garcomId, "QUEBRADA"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaMesaInexistente() {
        UUID garcomId = criarGarcom();

        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_COMANDA, UUID.randomUUID(), UUID.randomUUID(), garcomId, "ABERTA"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaGarcomInexistente() {
        UUID mesaId = criarMesa();

        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_COMANDA, UUID.randomUUID(), mesaId, UUID.randomUUID(), "ABERTA"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID criarMesa() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, ?)", id, 1, "LIVRE");
        return id;
    }

    private UUID criarGarcom() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into usuarios (id, nome, telefone, perfil) values (?, ?, ?, ?)",
                id, "Garcom", "11999999999", "GARCOM");
        return id;
    }

    private Object tipoDa(List<Map<String, Object>> colunas, String nome) {
        return colunas.stream()
                .filter(coluna -> coluna.get("column_name").equals(nome))
                .findFirst()
                .orElseThrow()
                .get("data_type");
    }
}
