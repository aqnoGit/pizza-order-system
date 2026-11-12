package com.pizzaria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

@AutoConfigureMockMvc
class PedidoControllerIntegrationTest extends PostgresIntegrationTest {

    private static final int NUMERO_MESA = 9201;

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
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, ?)", mesaId, NUMERO_MESA, "OCUPADA");
        jdbcTemplate.update("insert into usuarios (id, nome, telefone, perfil) values (?, ?, ?, ?)", garcomId,
                "garcom pedido", "11900000002", "GARCOM");
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, 'ABERTA')",
                comandaId, mesaId, garcomId);
        jdbcTemplate.update(
                "insert into produtos (id, nome, preco, categoria, disponivel) values (?, 'Calabresa', 42.50, 'PIZZA', true)",
                produtoId);
    }

    @AfterEach
    void limpar() {
        jdbcTemplate.update("delete from itens_pedido where pedido_id in (select id from pedidos where comanda_id = ?)",
                comandaId);
        jdbcTemplate.update("delete from pedidos where comanda_id = ?", comandaId);
        jdbcTemplate.update("delete from comandas where id = ?", comandaId);
        jdbcTemplate.update("delete from produtos where id = ?", produtoId);
        jdbcTemplate.update("delete from mesas where id = ?", mesaId);
        jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
    }

    @Test
    void garcomCriaPedidoPresencialComPrecoVigenteEPersiste() throws Exception {
        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 2)))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.origem").value("PRESENCIAL"))
                .andExpect(jsonPath("$.status").value("REGISTRADO"))
                .andExpect(jsonPath("$.comandaId").value(comandaId.toString()))
                .andExpect(jsonPath("$.valorTotal").value(85.0))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(42.5))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2));

        Integer itens = jdbcTemplate.queryForObject(
                "select count(*) from itens_pedido i join pedidos p on p.id = i.pedido_id where p.comanda_id = ? and p.origem = 'PRESENCIAL' and i.preco_unitario = 42.50",
                Integer.class, comandaId);
        assertThat(itens).isEqualTo(1);
    }

    @Test
    void quantidadeMenorQueUmRetorna400() throws Exception {
        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 0)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0]").value("itens[0].quantidade"));
    }

    @Test
    void listaDeItensVaziaRetorna400() throws Exception {
        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"itens\": []}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0]").value("itens"));
    }

    @Test
    void produtoInexistenteRetorna404() throws Exception {
        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(UUID.randomUUID(), 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void comandaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post(rota(UUID.randomUUID())).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 1)))
                .andExpect(status().isNotFound());
    }

    @Test
    void produtoIndisponivelRetorna422() throws Exception {
        jdbcTemplate.update("update produtos set disponivel = false where id = ?", produtoId);

        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 1)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void comandaFechadaRetorna422SemCriarPedido() throws Exception {
        jdbcTemplate.update("update comandas set status = 'FECHADA' where id = ?", comandaId);

        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 1)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422));

        Integer pedidos = jdbcTemplate.queryForObject("select count(*) from pedidos where comanda_id = ?",
                Integer.class, comandaId);
        assertThat(pedidos).isZero();
    }

    @Test
    void cozinhaNaoPodeCriarPedidoPelaComanda() throws Exception {
        mockMvc.perform(post(rota(comandaId)).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 1)))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(post(rota(comandaId)).contentType(MediaType.APPLICATION_JSON).content(corpo(produtoId, 1)))
                .andExpect(status().isUnauthorized());
    }

    private String rota(UUID comanda) {
        return "/comandas/" + comanda + "/pedidos";
    }

    private String corpo(UUID produto, int quantidade) {
        return "{\"itens\": [{\"produtoId\": \"" + produto + "\", \"quantidade\": " + quantidade + "}]}";
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(garcomId, "funcionario", "11900000000", perfil, null));
    }
}
