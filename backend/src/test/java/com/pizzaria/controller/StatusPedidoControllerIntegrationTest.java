package com.pizzaria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@AutoConfigureMockMvc
class StatusPedidoControllerIntegrationTest extends PostgresIntegrationTest {

    private static final String ROTA = "/pedidos/{id}/status";
    private static final int NUMERO_MESA = 9401;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private final UUID mesaId = UUID.randomUUID();
    private final UUID garcomId = UUID.randomUUID();
    private final UUID comandaId = UUID.randomUUID();
    private final UUID produtoId = UUID.randomUUID();

    @BeforeEach
    void prepararCenario() {
        limparPedidos();
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, 'OCUPADA')", mesaId, NUMERO_MESA);
        jdbcTemplate.update(
                "insert into usuarios (id, nome, telefone, perfil) values (?, 'garcom status', '11900000005', 'GARCOM')",
                garcomId);
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
        jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
    }

    @Test
    void registradoParaEmPreparo() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        enviar(pedido, "EM_PREPARO", Perfil.COZINHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_PREPARO"));
        assertThat(statusNoBanco(pedido)).isEqualTo("EM_PREPARO");
    }

    @Test
    void emPreparoParaPronto() throws Exception {
        UUID pedido = inserirPedido("EM_PREPARO");

        enviar(pedido, "PRONTO", Perfil.COZINHA)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PRONTO"));
        assertThat(statusNoBanco(pedido)).isEqualTo("PRONTO");
    }

    @Test
    void prontoParaEntregueComGarcom() throws Exception {
        UUID pedido = inserirPedido("PRONTO");

        enviar(pedido, "ENTREGUE", Perfil.GARCOM)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
        assertThat(statusNoBanco(pedido)).isEqualTo("ENTREGUE");
    }

    @Test
    void pularEtapaRetorna409SemAlterarStatus() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        enviar(pedido, "PRONTO", Perfil.COZINHA).andExpect(status().isConflict());
        enviar(pedido, "ENTREGUE", Perfil.COZINHA).andExpect(status().isConflict());
        assertThat(statusNoBanco(pedido)).isEqualTo("REGISTRADO");
    }

    @Test
    void pedidoEntregueRetorna409ParaQualquerStatus() throws Exception {
        UUID pedido = inserirPedido("ENTREGUE");

        for (String destino : new String[] {"REGISTRADO", "EM_PREPARO", "PRONTO", "ENTREGUE"}) {
            enviar(pedido, destino, Perfil.GARCOM).andExpect(status().isConflict());
        }
        assertThat(statusNoBanco(pedido)).isEqualTo("ENTREGUE");
    }

    @Test
    void voltarStatusRetorna409() throws Exception {
        UUID pedido = inserirPedido("EM_PREPARO");

        enviar(pedido, "REGISTRADO", Perfil.COZINHA).andExpect(status().isConflict());
        assertThat(statusNoBanco(pedido)).isEqualTo("EM_PREPARO");
    }

    @Test
    void pedidoInexistenteRetorna404() throws Exception {
        enviar(UUID.randomUUID(), "EM_PREPARO", Perfil.COZINHA).andExpect(status().isNotFound());
    }

    @Test
    void semStatusRetorna400() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        enviarCorpo(pedido, "{}", Perfil.COZINHA).andExpect(status().isBadRequest());
    }

    @Test
    void statusInvalidoRetorna400() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        enviar(pedido, "CANCELADO", Perfil.COZINHA).andExpect(status().isBadRequest());
        assertThat(statusNoBanco(pedido)).isEqualTo("REGISTRADO");
    }

    @Test
    void clienteRecebe403() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        enviar(pedido, "EM_PREPARO", Perfil.CLIENTE).andExpect(status().isForbidden());
        assertThat(statusNoBanco(pedido)).isEqualTo("REGISTRADO");
    }

    @Test
    void semTokenRetorna401() throws Exception {
        UUID pedido = inserirPedido("REGISTRADO");

        mockMvc.perform(patch(ROTA, pedido).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"EM_PREPARO\"}"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions enviar(UUID pedidoId, String novoStatus, Perfil perfil) throws Exception {
        return enviarCorpo(pedidoId, "{\"status\":\"" + novoStatus + "\"}", perfil);
    }

    private ResultActions enviarCorpo(UUID pedidoId, String corpo, Perfil perfil) throws Exception {
        return mockMvc.perform(patch(ROTA, pedidoId)
                .header(HttpHeaders.AUTHORIZATION, bearer(perfil))
                .contentType(MediaType.APPLICATION_JSON)
                .content(corpo));
    }

    private void limparPedidos() {
        jdbcTemplate.update("delete from itens_pedido");
        jdbcTemplate.update("delete from pedidos");
    }

    private UUID inserirPedido(String status) {
        UUID pedidoId = UUID.randomUUID();
        jdbcTemplate.update(
                "insert into pedidos (id, comanda_id, origem, status, registrado_em, valor_total) values (?, ?, 'PRESENCIAL', ?, now(), 42.50)",
                pedidoId, comandaId, status);
        jdbcTemplate.update(
                "insert into itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario) values (?, ?, ?, 1, 42.50)",
                UUID.randomUUID(), pedidoId, produtoId);
        return pedidoId;
    }

    private String statusNoBanco(UUID pedidoId) {
        return jdbcTemplate.queryForObject("select status from pedidos where id = ?", String.class, pedidoId);
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(garcomId, "funcionario", "11900000000", perfil, null));
    }
}
