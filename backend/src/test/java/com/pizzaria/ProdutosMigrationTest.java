package com.pizzaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class ProdutosMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_PRODUTO =
            "insert into produtos (id, nome, descricao, preco, categoria) values (?, ?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaProdutosPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type, is_nullable from information_schema.columns "
                        + "where table_name = 'produtos' order by column_name");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder("id", "nome", "descricao", "preco", "categoria", "disponivel");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "nome")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "descricao")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "preco")).isEqualTo("numeric");
        assertThat(tipoDa(colunas, "categoria")).isEqualTo("character varying");
        assertThat(tipoDa(colunas, "disponivel")).isEqualTo("boolean");
    }

    @Test
    void descricaoEAUnicaColunaOpcional() {
        List<String> opcionais = jdbcTemplate.queryForList(
                "select column_name from information_schema.columns "
                        + "where table_name = 'produtos' and is_nullable = 'YES'",
                String.class);

        assertThat(opcionais).containsExactly("descricao");
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'produtos' and column_name = 'id' and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void produtoNasceDisponivelPorPadrao() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(INSERIR_PRODUTO, id, "Margherita", null, new BigDecimal("39.90"), "PIZZA");

        Boolean disponivel = jdbcTemplate.queryForObject(
                "select disponivel from produtos where id = ?", Boolean.class, id);

        assertThat(disponivel).isTrue();
    }

    @Test
    void rejeitaPrecoNegativo() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_PRODUTO, UUID.randomUUID(), "Calabresa", null, new BigDecimal("-1.00"), "PIZZA"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaNomeNulo() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_PRODUTO, UUID.randomUUID(), null, null, new BigDecimal("10.00"), "BEBIDA"))
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
