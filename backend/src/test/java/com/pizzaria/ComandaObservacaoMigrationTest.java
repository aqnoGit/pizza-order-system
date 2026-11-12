package com.pizzaria;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;

class ComandaObservacaoMigrationTest extends PostgresIntegrationTest {

    private static final int TAMANHO_MAXIMO = 500;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void colunaObservacaoEVarcharDe500ComNulosPermitidos() {
        Map<String, Object> coluna = jdbcTemplate.queryForMap(
                "select data_type, character_maximum_length, is_nullable from information_schema.columns "
                        + "where table_name = 'comandas' and column_name = 'observacao'");

        assertThat(coluna.get("data_type")).isEqualTo("character varying");
        assertThat(coluna.get("character_maximum_length")).isEqualTo(TAMANHO_MAXIMO);
        assertThat(coluna.get("is_nullable")).isEqualTo("YES");
    }

    @Test
    void comandaSemObservacaoFicaComObservacaoNula() {
        UUID comandaId = criarComanda(null);

        String observacao = jdbcTemplate.queryForObject(
                "select observacao from comandas where id = ?", String.class, comandaId);

        assertThat(observacao).isNull();
    }

    @Test
    void aceitaObservacaoComExatamente500Caracteres() {
        String observacao = "a".repeat(TAMANHO_MAXIMO);

        UUID comandaId = criarComanda(observacao);

        String salva = jdbcTemplate.queryForObject(
                "select observacao from comandas where id = ?", String.class, comandaId);
        assertThat(salva).isEqualTo(observacao);
    }

    @Test
    void rejeitaObservacaoComMaisDe500Caracteres() {
        String observacaoLonga = "a".repeat(TAMANHO_MAXIMO + 1);

        assertThatThrownBy(() -> criarComanda(observacaoLonga))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID criarComanda(String observacao) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into comandas (id, mesa_id, garcom_id, status, observacao) values (?, ?, ?, 'ABERTA', ?)",
                id, criarMesa(), criarGarcom(), observacao);
        return id;
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
}
