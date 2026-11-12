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
class MesaControllerIntegrationTest extends PostgresIntegrationTest {

    private static final int NUMERO_LIVRE = 9001;
    private static final int NUMERO_OCUPADA = 9002;
    private static final String INSERIR_MESA = "insert into mesas (id, numero, status) values (?, ?, ?)";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    private final UUID idLivre = UUID.randomUUID();
    private final UUID idOcupada = UUID.randomUUID();

    @BeforeEach
    void criarMesas() {
        jdbcTemplate.update(INSERIR_MESA, idLivre, NUMERO_LIVRE, "LIVRE");
        jdbcTemplate.update(INSERIR_MESA, idOcupada, NUMERO_OCUPADA, "OCUPADA");
    }

    @AfterEach
    void removerMesas() {
        jdbcTemplate.update("delete from mesas where id in (?, ?)", idLivre, idOcupada);
    }

    @Test
    void garcomRecebeMesasComStatusGravadoNoBanco() throws Exception {
        mockMvc.perform(get("/mesas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == '" + idLivre + "')].status").value("LIVRE"))
                .andExpect(jsonPath("$[?(@.id == '" + idLivre + "')].numero").value(NUMERO_LIVRE))
                .andExpect(jsonPath("$[?(@.id == '" + idOcupada + "')].status").value("OCUPADA"))
                .andExpect(jsonPath("$[?(@.id == '" + idOcupada + "')].numero").value(NUMERO_OCUPADA));
    }

    @Test
    void semTokenRetorna401() throws Exception {
        mockMvc.perform(get("/mesas")).andExpect(status().isUnauthorized());
    }

    @Test
    void cozinhaRecebe403() throws Exception {
        mockMvc.perform(get("/mesas").header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isForbidden());
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(UUID.randomUUID(), "funcionario", "11900000000", perfil, null));
    }
}
