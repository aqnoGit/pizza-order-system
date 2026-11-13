package com.pizzaria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pizzaria.PostgresIntegrationTest;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.service.ImpressoraClient;
import com.pizzaria.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
class ComprovanteControllerIntegrationTest extends PostgresIntegrationTest {

    private static final int NUMERO_MESA = 9301;
    private static final String URL_IMPRIMIR = "/comprovantes/{id}/imprimir";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private ImpressoraClient impressoraClient;

    private final UUID mesaId = UUID.randomUUID();
    private final UUID garcomId = UUID.randomUUID();
    private final UUID comandaId = UUID.randomUUID();

    @BeforeEach
    void criarComanda() {
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, ?)", mesaId, NUMERO_MESA, "OCUPADA");
        jdbcTemplate.update("insert into usuarios (id, nome, telefone, perfil) values (?, ?, ?, ?)", garcomId,
                "garcom comprovante", "11900000093", "GARCOM");
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, ?)", comandaId,
                mesaId, garcomId, "ABERTA");
    }

    @AfterEach
    void limpar() {
        jdbcTemplate.update("delete from comandas where id = ?", comandaId);
        jdbcTemplate.update("delete from mesas where id = ?", mesaId);
        jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
    }

    @Test
    void garcomImprimeComprovanteERecebe200() throws Exception {
        mockMvc.perform(post(URL_IMPRIMIR, comandaId).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isOk());

        verify(impressoraClient).enviar(any(byte[].class));
    }

    @Test
    void adminImprimeComprovanteERecebe200() throws Exception {
        mockMvc.perform(post(URL_IMPRIMIR, comandaId).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void comandaInexistenteRetorna404SemEnviarParaImpressora() throws Exception {
        mockMvc.perform(post(URL_IMPRIMIR, UUID.randomUUID()).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.GARCOM)))
                .andExpect(status().isNotFound());

        verifyNoInteractions(impressoraClient);
    }

    @Test
    void cozinhaRecebe403() throws Exception {
        mockMvc.perform(post(URL_IMPRIMIR, comandaId).header(HttpHeaders.AUTHORIZATION, bearer(Perfil.COZINHA)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(impressoraClient);
    }

    @Test
    void semTokenRecebe401() throws Exception {
        mockMvc.perform(post(URL_IMPRIMIR, comandaId)).andExpect(status().isUnauthorized());
    }

    private String bearer(Perfil perfil) {
        return "Bearer " + jwtService.gerarToken(new Usuario(garcomId, "funcionario", "11900000000", perfil, null));
    }
}
