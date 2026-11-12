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

class ItensPedidoMigrationTest extends PostgresIntegrationTest {

    private static final String INSERIR_ITEM =
            "insert into itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario) values (?, ?, ?, ?, ?)";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void tabelaItensPedidoPossuiColunasComOsTiposEsperados() {
        List<Map<String, Object>> colunas = jdbcTemplate.queryForList(
                "select column_name, data_type from information_schema.columns "
                        + "where table_name = 'itens_pedido'");

        assertThat(colunas).extracting(coluna -> coluna.get("column_name"))
                .containsExactlyInAnyOrder("id", "pedido_id", "produto_id", "quantidade", "preco_unitario",
                        "observacao");
        assertThat(tipoDa(colunas, "id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "pedido_id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "produto_id")).isEqualTo("uuid");
        assertThat(tipoDa(colunas, "quantidade")).isEqualTo("integer");
        assertThat(tipoDa(colunas, "preco_unitario")).isEqualTo("numeric");
        assertThat(tipoDa(colunas, "observacao")).isEqualTo("character varying");
    }

    @Test
    void idEChavePrimaria() {
        Integer chaves = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.key_column_usage "
                        + "where table_name = 'itens_pedido' and column_name = 'id' "
                        + "and constraint_name like '%pkey'",
                Integer.class);

        assertThat(chaves).isEqualTo(1);
    }

    @Test
    void aceitaItemValido() {
        UUID id = UUID.randomUUID();

        jdbcTemplate.update(INSERIR_ITEM, id, criarPedido(), criarProduto(), 2, new BigDecimal("39.90"));

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from itens_pedido where id = ?", Integer.class, id);
        assertThat(total).isEqualTo(1);
    }

    @Test
    void rejeitaPedidoInexistente() {
        UUID produtoId = criarProduto();

        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_ITEM, UUID.randomUUID(), UUID.randomUUID(), produtoId, 1, new BigDecimal("10.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaProdutoInexistente() {
        UUID pedidoId = criarPedido();

        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_ITEM, UUID.randomUUID(), pedidoId, UUID.randomUUID(), 1, new BigDecimal("10.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaQuantidadeZeroOuNegativa() {
        UUID pedidoId = criarPedido();
        UUID produtoId = criarProduto();

        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_ITEM, UUID.randomUUID(), pedidoId, produtoId, 0, new BigDecimal("10.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_ITEM, UUID.randomUUID(), pedidoId, produtoId, -1, new BigDecimal("10.00")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void rejeitaPrecoUnitarioNegativo() {
        UUID pedidoId = criarPedido();
        UUID produtoId = criarProduto();

        assertThatThrownBy(() -> jdbcTemplate.update(
                INSERIR_ITEM, UUID.randomUUID(), pedidoId, produtoId, 1, new BigDecimal("-0.01")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void apagarPedidoApagaSeusItens() {
        UUID pedidoId = criarPedido();
        jdbcTemplate.update(INSERIR_ITEM, UUID.randomUUID(), pedidoId, criarProduto(), 1, new BigDecimal("10.00"));

        jdbcTemplate.update("delete from pedidos where id = ?", pedidoId);

        Integer total = jdbcTemplate.queryForObject(
                "select count(*) from itens_pedido where pedido_id = ?", Integer.class, pedidoId);
        assertThat(total).isZero();
    }

    @Test
    void naoPermiteApagarProdutoComItens() {
        UUID produtoId = criarProduto();
        jdbcTemplate.update(INSERIR_ITEM, UUID.randomUUID(), criarPedido(), produtoId, 1, new BigDecimal("10.00"));

        assertThatThrownBy(() -> jdbcTemplate.update("delete from produtos where id = ?", produtoId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private UUID criarPedido() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("insert into pedidos (id, origem, status) values (?, 'PRESENCIAL', 'REGISTRADO')", id);
        return id;
    }

    private UUID criarProduto() {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into produtos (id, nome, preco, categoria) values (?, 'Margherita', 39.90, 'PIZZA')", id);
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
