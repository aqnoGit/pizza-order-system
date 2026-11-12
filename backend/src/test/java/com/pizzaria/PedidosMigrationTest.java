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

class PedidosMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_PEDIDO =
            "insert into pedidos (id, comanda_id, cliente_id, origem, status) values (?, ?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaPedidosPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type, is_nullable from information_schema.columns "
                        + "where table_name = 'pedidos'");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder(
                        "id", "comanda_id", "cliente_id", "origem", "status", "registrado_em",
                        "inicio_preparo", "finalizado_em", "valor_total");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "valor_total")).isEqualTo("numeric");
        assertThat(anulavel(colunas, "comanda_id")).isEqualTo("YES");
        assertThat(anulavel(colunas, "cliente_id")).isEqualTo("YES");
        assertThat(anulavel(colunas, "inicio_preparo")).isEqualTo("YES");
        assertThat(anulavel(colunas, "finalizado_em")).isEqualTo("YES");
        assertThat(anulavel(colunas, "origem")).isEqualTo("NO");
        assertThat(anulavel(colunas, "status")).isEqualTo("NO");
        assertThat(anulavel(colunas, "valor_total")).isEqualTo("NO");
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'pedidos' and column_name = 'id' and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void aceitaPedidoSemComandaENemCliente() {
        UUID pedidoId = UUID.randomUUID();

        jdbcTemplate.update(INSERIR_PEDIDO, pedidoId, null, null, "ENTREGA", "REGISTRADO");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from pedidos where id = ?", Integer.class, pedidoId);
        assertThat(total).isEqualTo(1);
    }

    @Test
    void aceitaPedidoComComandaECliente() {
        UUID usuarioId = criarUsuario();
        UUID comandaId = criarComanda(usuarioId);

        jdbcTemplate.update(INSERIR_PEDIDO, UUID.randomUUID(), comandaId, usuarioId, "PRESENCIAL", "EM_PREPARO");

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from pedidos where comanda_id = ?", Integer.class, comandaId);
        assertThat(total).isEqualTo(1);
    }

    @Test
    void aceitaTodasAsOrigensEStatusValidos() {
        UUID usuarioId = criarUsuario();
        for (String origem : List.of("PRESENCIAL", "ENTREGA")) {
            for (String status : List.of("REGISTRADO", "EM_PREPARO", "PRONTO", "ENTREGUE")) {
                jdbcTemplate.update(INSERIR_PEDIDO, UUID.randomUUID(), null, usuarioId, origem, status);
            }
        }

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from pedidos where cliente_id = ?", Integer.class, usuarioId);
        assertThat(total).isEqualTo(8);
    }

    @Test
    void rejeitaOrigemInvalida() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_PEDIDO, UUID.randomUUID(), null, null, "DRIVE_THRU", "REGISTRADO"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaStatusInvalido() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_PEDIDO, UUID.randomUUID(), null, null, "ENTREGA", "CANCELADO"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaComandaInexistente() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_PEDIDO, UUID.randomUUID(), UUID.randomUUID(), null, "PRESENCIAL", "REGISTRADO"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaClienteInexistente() {
        assertThatThrownBy(() -> jdbcTemplate.update(
                        INSERIR_PEDIDO, UUID.randomUUID(), null, UUID.randomUUID(), "ENTREGA", "REGISTRADO"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID criarUsuario() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into usuarios (id, nome, telefone, perfil) values (?, ?, ?, ?)",
                id, "Usuario", "11999999999", "GARCOM");
        return id;
    }

    private UUID criarComanda(UUID garcomId) {
        UUID mesaId = UUID.randomUUID();
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, ?)", mesaId, 1, "LIVRE");
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, ?)",
                id, mesaId, garcomId, "ABERTA");
        return id;
    }

    private Object tipoDa(List<Map<String, Object>> colunas, String nome) {
        return colunaDe(colunas, nome).get("data_type");
    }

    private Object anulavel(List<Map<String, Object>> colunas, String nome) {
        return colunaDe(colunas, nome).get("is_nullable");
    }

    private Map<String, Object> colunaDe(List<Map<String, Object>> colunas, String nome) {
        return colunas.stream()
                .filter(coluna -> coluna.get("column_name").equals(nome))
                .findFirst()
                .orElseThrow();
    }
}
