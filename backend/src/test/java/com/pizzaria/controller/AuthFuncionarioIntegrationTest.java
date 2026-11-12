package com.pizzaria.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pizzaria.PostgresIntegrationTest;
import com.pizzaria.entity.Perfil;
import com.pizzaria.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@AutoConfigureMockMvc
class AuthFuncionarioIntegrationTest extends PostgresIntegrationTest {

    private static final String SENHA = "senha-correta";
    private static final String INSERIR_USUARIO =
            "insert into usuarios (id, nome, telefone, perfil, credenciais) values (?, ?, ?, ?, ?)";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private final String sufixo = UUID.randomUUID().toString();
    private final String garcom = "garcom." + sufixo;
    private final String cozinha = "cozinha." + sufixo;
    private final String cliente = "cliente." + sufixo;
    private final String admin = "admin." + sufixo;

    @BeforeEach
    void criarUsuarios() {
        inserir(garcom, "11911111111", Perfil.GARCOM);
        inserir(cozinha, "11922222222", Perfil.COZINHA);
        inserir(cliente, "11933333333", Perfil.CLIENTE);
        inserir(admin, "11944444444", Perfil.ADMIN);
    }

    @AfterEach
    void limparUsuarios() {
        jdbcTemplate.update("delete from usuarios where nome in (?, ?, ?, ?)", garcom, cozinha, cliente, admin);
    }

    @Test
    void garcomComSenhaCorretaRecebeTokenComPerfilGarcom() throws Exception {
        String token = logar(garcom, SENHA);

        assertThat(jwtService.lerClaims(token).get("perfil", String.class)).isEqualTo("GARCOM");
    }

    @Test
    void cozinhaComSenhaCorretaRecebeTokenComPerfilCozinha() throws Exception {
        String token = logar(cozinha, SENHA);

        assertThat(jwtService.lerClaims(token).get("perfil", String.class)).isEqualTo("COZINHA");
    }

    @Test
    void adminComSenhaCorretaRecebeTokenComPerfilAdmin() throws Exception {
        String token = logar(admin, SENHA);

        assertThat(jwtService.lerClaims(token).get("perfil", String.class)).isEqualTo("ADMIN");
    }

    @Test
    void clienteNaoPodeLogarPorEsteEndpoint() throws Exception {
        tentarLogar(cliente, SENHA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais incorretas"));
    }

    @Test
    void senhaErradaRetorna401() throws Exception {
        tentarLogar(garcom, "senha-errada")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais incorretas"));
    }

    @Test
    void usuarioInexistenteRetorna401ComAMesmaMensagem() throws Exception {
        tentarLogar("fantasma", SENHA)
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensagem").value("Credenciais incorretas"));
    }

    @Test
    void senhaNuncaFicaGravadaEmTextoPuro() {
        String credenciais = jdbcTemplate.queryForObject(
                "select credenciais from usuarios where nome = ?", String.class, garcom);

        assertThat(credenciais).isNotEqualTo(SENHA);
        assertThat(passwordEncoder.matches(SENHA, credenciais)).isTrue();
    }

    private void inserir(String nome, String telefone, Perfil perfil) {
        jdbcTemplate.update(INSERIR_USUARIO, UUID.randomUUID(), nome, telefone, perfil.name(),
                passwordEncoder.encode(SENHA));
    }

    private String logar(String usuario, String senha) throws Exception {
        MvcResult resultado = tentarLogar(usuario, senha).andExpect(status().isOk()).andReturn();
        JsonNode corpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        return corpo.get("token").asText();
    }

    private org.springframework.test.web.servlet.ResultActions tentarLogar(String usuario, String senha)
            throws Exception {
        String corpo = objectMapper.writeValueAsString(
                java.util.Map.of("usuario", usuario, "senha", senha));
        return mockMvc.perform(post("/auth/funcionario").contentType(MediaType.APPLICATION_JSON).content(corpo));
    }
}
