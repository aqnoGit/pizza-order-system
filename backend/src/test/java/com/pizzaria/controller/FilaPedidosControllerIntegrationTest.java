package com.pizzaria.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pizzaria.PostgresIntegrationTest;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class FilaPedidosControllerIntegrationTest extends PostgresIntegrationTest {

    private static final String ROTA = "/pedidos/fila";
    private static final int NUMERO_MESA = 9301;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private final UUID mesaId = UUID.randomUUID();
    private final UUID garcomId = UUID.randomUUID();
    private final UUID clienteId = UUID.randomUUID();
    private final UUID comandaId = UUID.randomUUID();
    private final UUID produtoId = UUID.randomUUID();

    @BeforeEach
    void prepararCenario() {
        limparPedidos();
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, 'OCUPADA')", mesaId, NUMERO_MESA);
        jdbcTemplate.update(
                "insert into usuarios (id, nome, telefone, perfil) values (?, 'garcom fila', '11900000003', 'GARCOM')",
                garcomId);
        jdbcTemplate.update(
                "insert into usuarios (id, nome, telefone, perfil) values (?, 'cliente fila', '11900000004', 'CLIENTE')",
                clienteId);
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, 'ABERTA')",
                comandaId, mesaId, garcomId);
        jdbcTemplate.update(
                "insert into produtos (id, nome, preco, categoria, disponivel) values (?, 'Calabresa', 42.50, 'PIZZA', true)",
                produtoId);
    }

    @AfterEach
    void limpar() {
        limparPedidos();
        jdbcTemplate.update("delete from comandas where id = ?", comandaId);
        jdbcTemplate.update("delete from produtos where id = ?", produtoId);
        jdbcTemplate.update("delete from mesas where id = ?", mesaId);
        jdbcTemplate.update("delete from usuarios where id in (?, ?)", garcomId, clienteId);
    }

    @Test
    void retornaApenasPedidosRegistrados() throws Exception {
        UUID registrado = inserirPedidoPresencial("REGISTRADO", "2026-01-01 10:00:00");
        inserirPedidoPresencial("EM_PREPARO", "2026-01-01 10:01:00");
        inserirPedidoPresencial("PRONTO", "2026-01-01 10:02:00");
        inserirPedidoPresencial("ENTREGUE", "2026-01-01 10:03:00");

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(registrado.toString()));
    }

    @Test
    void ordenaPorRegistradoEmCrescente() throws Exception {
        UUID maisNovo = inserirPedidoPresencial("REGISTRADO", "2026-01-01 12:00:00");
        UUID maisAntigo = inserirPedidoPresencial("REGISTRADO", "2026-01-01 09:00:00");
        UUID intermediario = inserirPedidoPresencial("REGISTRADO", "2026-01-01 10:30:00");

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(maisAntigo.toString()))
                .andExpect(jsonPath("$[1].id").value(intermediario.toString()))
                .andExpect(jsonPath("$[2].id").value(maisNovo.toString()));
    }

    @Test
    void pedidoPresencialTrazOrigemENumeroDaMesa() throws Exception {
        inserirPedidoPresencial("REGISTRADO", "2026-01-01 10:00:00");

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(jsonPath("$[0].origem").value("PRESENCIAL"))
                .andExpect(jsonPath("$[0].numeroMesa").value(NUMERO_MESA));
    }

    @Test
    void pedidoDeEntregaTemNumeroMesaNulo() throws Exception {
        UUID entrega = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into pedidos (id, cliente_id, origem, status, registrado_em, valor_total) values (?, ?, 'ENTREGA', 'REGISTRADO', '2026-01-01 10:00:00', 42.50)",
                entrega, clienteId);
        inserirItem(entrega, 1, null);

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].origem").value("ENTREGA"))
                .andExpect(jsonPath("$[0].numeroMesa").isEmpty());
    }

    @Test
    void filaMistaTrazOrigemDeCadaPedidoNaOrdemDeRegistro() throws Exception {
        inserirPedidoPresencial("REGISTRADO", "2026-01-01 10:00:00");
        UUID entrega = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into pedidos (id, cliente_id, origem, status, registrado_em, valor_total) values (?, ?, 'ENTREGA', 'REGISTRADO', '2026-01-01 10:05:00', 42.50)",
                entrega, clienteId);
        inserirItem(entrega, 1, null);

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].origem").value("PRESENCIAL"))
                .andExpect(jsonPath("$[0].numeroMesa").value(NUMERO_MESA))
                .andExpect(jsonPath("$[1].origem").value("ENTREGA"))
                .andExpect(jsonPath("$[1].numeroMesa").isEmpty());
    }

    @Test
    void trazItensComProdutoEQuantidade() throws Exception {
        UUID pedido = inserirPedidoPresencial("REGISTRADO", "2026-01-01 10:00:00");
        inserirItem(pedido, 3, "sem cebola");

        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(jsonPath("$[0].itens.length()").value(2))
                .andExpect(jsonPath("$[0].itens[0].produtoId").value(produtoId.toString()))
                .andExpect(jsonPath("$[0].itens[0].nomeProduto").value("Calabresa"))
                .andExpect(jsonPath("$[0].itens[?(@.quantidade == 3 && @.observacao == 'sem cebola')]").isNotEmpty());
    }

    @Test
    void filaVaziaRetorna200ComListaVazia() throws Exception {
        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void adminAcessaAFila() throws Exception {
        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void garcomNaoAcessaAFila() throws Exception {
        mockMvc.perform(get(ROTA).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get(ROTA)).andExpect(status().isUnauthorized());
    }

    private void limparPedidos() {
        jdbcTemplate.update("delete from itens_pedido");
        jdbcTemplate.update("delete from pedidos");
    }

    private UUID inserirPedidoPresencial(String status, String registradoEm) {
        UUID pedidoId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into pedidos (id, comanda_id, origem, status, registrado_em, valor_total) values (?, ?, 'PRESENCIAL', ?, ?::timestamp, 42.50)",
                pedidoId, comandaId, status, registradoEm);
        inserirItem(pedidoId, 1, null);
        return pedidoId;
    }

    private void inserirItem(UUID pedidoId, int quantidade, String observacao) {
        jdbcTemplate.update(
                "insert into itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario, observacao) values (?, ?, ?, ?, 42.50, ?)",
                UUID.randomUUID(), pedidoId, produtoId, quantidade, observacao);
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(garcomId, "funcionario", "11900000000", perfil, null));
    }
}
