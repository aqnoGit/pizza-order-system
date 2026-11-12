package com.pizzaria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pizzaria.PostgresIntegrationTest;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.service.JwtService;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
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
class ComandaControllerIntegrationTest extends PostgresIntegrationTest {

    private static final int NUMERO_MESA = 9101;
    private static final String CONSULTA_COMANDAS_ABERTAS =
            "select count(*) from comandas where mesa_id = ? and garcom_id = ? and status = 'ABERTA'";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private final UUID mesaId = UUID.randomUUID();
    private final UUID garcomId = UUID.randomUUID();
    private final List<UUID> produtosCriados = new ArrayList<>();

    @BeforeEach
    void criarMesaEGarcom() {
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, ?)", mesaId, NUMERO_MESA, "LIVRE");
        jdbcTemplate.update("insert into usuarios (id, nome, telefone, perfil) values (?, ?, ?, ?)", garcomId,
                "garcom teste", "11900000001", "GARCOM");
    }

    @AfterEach
    void limpar() {
        jdbcTemplate.update("delete from itens_pedido where pedido_id in (select p.id from pedidos p join comandas c on c.id = p.comanda_id where c.mesa_id = ?)", mesaId);
        jdbcTemplate.update("delete from pedidos where comanda_id in (select id from comandas where mesa_id = ?)", mesaId);
        jdbcTemplate.update("delete from comandas where mesa_id = ?", mesaId);
        produtosCriados.forEach(id -> jdbcTemplate.update("delete from produtos where id = ?", id));
        jdbcTemplate.update("delete from mesas where id = ?", mesaId);
        jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
    }

    @Test
    void garcomCriaComandaAbertaPersistidaComOGarcomDoToken() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.valorTotal").value(0))
                .andExpect(jsonPath("$.mesaId").value(mesaId.toString()))
                .andExpect(jsonPath("$.garcomId").value(garcomId.toString()))
                .andExpect(jsonPath("$.abertaEm").isNotEmpty())
                .andExpect(jsonPath("$.fechadaEm").doesNotExist());

        Integer total = jdbcTemplate.queryForObject(CONSULTA_COMANDAS_ABERTAS, Integer.class, mesaId, garcomId);
        assertThat(total).isEqualTo(1);
        assertThat(statusDaMesa()).isEqualTo("OCUPADA");
    }

    @Test
    void mesaInexistenteNaoAlteraNenhumaMesa() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(UUID.randomUUID().toString())))
                .andExpect(status().isNotFound());

        assertThat(statusDaMesa()).isEqualTo("LIVRE");
    }

    @Test
    void adminTambemCriaComanda() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isCreated());
    }

    @Test
    void semMesaIdRetorna400() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos[0]").value("mesaId"));
    }

    @Test
    void mesaInexistenteRetorna404() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(UUID.randomUUID().toString())))
                .andExpect(status().isNotFound());
    }

    @Test
    void mesaComComandaAbertaRetorna409() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        Integer total = jdbcTemplate.queryForObject(CONSULTA_COMANDAS_ABERTAS, Integer.class, mesaId, garcomId);
        assertThat(total).isEqualTo(1);
    }

    @Test
    void mesaSoComComandaFechadaCriaNovaComanda() throws Exception {
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, 'FECHADA')",
                UUID.randomUUID(), mesaId, garcomId);

        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isCreated());
    }

    @Test
    void cozinhaRetorna403() throws Exception {
        mockMvc.perform(post("/comandas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA))
                        .contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isForbidden());
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(post("/comandas").contentType(MediaType.APPLICATION_JSON).content(corpo(mesaId.toString())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void garcomConsultaComandaSemItens() throws Exception {
        UUID comandaId = UUID.randomUUID();
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, 'ABERTA')",
                comandaId, mesaId, garcomId);

        mockMvc.perform(get("/comandas/" + comandaId).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(comandaId.toString()))
                .andExpect(jsonPath("$.mesaId").value(mesaId.toString()))
                .andExpect(jsonPath("$.status").value("ABERTA"))
                .andExpect(jsonPath("$.abertaEm").isNotEmpty())
                .andExpect(jsonPath("$.valorTotal").value(0))
                .andExpect(jsonPath("$.itens").isEmpty());
    }

    @Test
    void consultaDeComandaInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/comandas/" + UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isNotFound());
    }

    @Test
    void consultaDeComandaSemTokenRetorna401() throws Exception {
        mockMvc.perform(get("/comandas/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    @Test
    void cozinhaNaoConsultaComanda() throws Exception {
        mockMvc.perform(get("/comandas/" + UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isForbidden());
    }

    @Test
    void garcomFechaComandaAberta() throws Exception {
        UUID comandaId = inserirComanda("ABERTA");

        mockMvc.perform(patch("/comandas/" + comandaId + "/fechar").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FECHADA"))
                .andExpect(jsonPath("$.fechadaEm").isNotEmpty());

        String statusPersistido = jdbcTemplate.queryForObject("select status from comandas where id = ?", String.class,
                comandaId);
        assertThat(statusPersistido).isEqualTo("FECHADA");
    }

    @Test
    void fecharComandaGravaOValorTotalSomadoDosItensDeTodosOsPedidos() throws Exception {
        UUID comandaId = inserirComanda("ABERTA");
        UUID produtoId = inserirProduto();
        inserirPedidoComItens(comandaId, produtoId, "40.00", 2);
        inserirPedidoComItens(comandaId, produtoId, "12.50", 1);

        mockMvc.perform(patch("/comandas/" + comandaId + "/fechar").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(92.5));

        BigDecimal valorPersistido = jdbcTemplate.queryForObject("select valor_total from comandas where id = ?",
                BigDecimal.class, comandaId);
        assertThat(valorPersistido).isEqualByComparingTo("92.50");
    }

    @Test
    void fecharComandaSemItensGravaValorTotalZero() throws Exception {
        UUID comandaId = inserirComanda("ABERTA");

        mockMvc.perform(patch("/comandas/" + comandaId + "/fechar").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotal").value(0));
    }

    @Test
    void fecharComandaJaFechadaRetorna409() throws Exception {
        UUID comandaId = inserirComanda("FECHADA");

        mockMvc.perform(patch("/comandas/" + comandaId + "/fechar").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensagem").value("Comanda ja esta fechada"));
    }

    @Test
    void fecharComandaInexistenteRetorna404() throws Exception {
        mockMvc.perform(patch("/comandas/" + UUID.randomUUID() + "/fechar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isNotFound());
    }

    @Test
    void fecharComandaSemTokenRetorna401() throws Exception {
        mockMvc.perform(patch("/comandas/" + UUID.randomUUID() + "/fechar")).andExpect(status().isUnauthorized());
    }

    @Test
    void cozinhaNaoFechaComanda() throws Exception {
        mockMvc.perform(patch("/comandas/" + UUID.randomUUID() + "/fechar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isForbidden());
    }

    private UUID inserirComanda(String status) {
        UUID comandaId = UUID.randomUUID();
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, ?)", comandaId,
                mesaId, garcomId, status);
        return comandaId;
    }

    private UUID inserirProduto() {
        UUID produtoId = UUID.randomUUID();
        jdbcTemplate.update("insert into produtos (id, nome, preco, categoria) values (?, ?, ?, ?)", produtoId,
                "pizza teste", new BigDecimal("40.00"), "PIZZA");
        produtosCriados.add(produtoId);
        return produtoId;
    }

    private void inserirPedidoComItens(UUID comandaId, UUID produtoId, String precoUnitario, int quantidade) {
        UUID pedidoId = UUID.randomUUID();
        jdbcTemplate.update("insert into pedidos (id, comanda_id, origem, status) values (?, ?, 'PRESENCIAL', 'REGISTRADO')",
                pedidoId, comandaId);
        jdbcTemplate.update(
                "insert into itens_pedido (id, pedido_id, produto_id, quantidade, preco_unitario) values (?, ?, ?, ?, ?)",
                UUID.randomUUID(), pedidoId, produtoId, quantidade, new BigDecimal(precoUnitario));
    }

    private String statusDaMesa() {
        return jdbcTemplate.queryForObject("select status from mesas where id = ?", String.class, mesaId);
    }

    private String corpo(String mesa) {
        return "{\"mesaId\": \"" + mesa + "\"}";
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(garcomId, "funcionario", "11900000000", perfil, null));
    }
}
