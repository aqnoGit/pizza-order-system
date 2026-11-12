package com.pizzaria.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pizzaria.config.SecurityConfig;
import com.pizzaria.dto.TokenResponse;
import com.pizzaria.exception.CredenciaisInvalidasException;
import com.pizzaria.service.AuthService;
import com.pizzaria.service.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AuthControllerTest {

    private static final String CORPO_VALIDO = "{\"usuario\":\"maria\",\"senha\":\"segredo\"}";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void retorna200ComTokenQuandoLoginValido() throws Exception {
        when(authService.loginFuncionario(any())).thenReturn(new TokenResponse("token-jwt"));

        mockMvc.perform(post("/auth/funcionario").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-jwt"));
    }

    @Test
    void retorna401QuandoCredenciaisInvalidas() throws Exception {
        when(authService.loginFuncionario(any())).thenThrow(new CredenciaisInvalidasException());

        mockMvc.perform(post("/auth/funcionario").contentType(MediaType.APPLICATION_JSON).content(CORPO_VALIDO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.mensagem").value("Credenciais incorretas"));
    }

    @Test
    void retorna400QuandoCamposEmBranco() throws Exception {
        mockMvc.perform(post("/auth/funcionario").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"usuario\":\"\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }
}
