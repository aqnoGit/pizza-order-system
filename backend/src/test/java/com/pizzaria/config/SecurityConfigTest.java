package com.pizzaria.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pizzaria.controller.AuthController;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.service.AuthService;
import com.pizzaria.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SecurityConfigTest.JwtServiceReal.class})
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final String ROTA_PROTEGIDA = "/rota-protegida";

    @TestConfiguration
    static class JwtServiceReal {

        @Bean
        JwtService jwtService(JwtProperties propriedades) {
            return new JwtService(propriedades);
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockBean
    private AuthService authService;

    @Test
    void retorna401QuandoSemToken() throws Exception {
        mockMvc.perform(get(ROTA_PROTEGIDA)).andExpect(status().isUnauthorized());
    }

    @Test
    void retorna401QuandoTokenInvalido() throws Exception {
        mockMvc.perform(get(ROTA_PROTEGIDA).header("Authorization", "Bearer invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void permiteAcessoQuandoTokenValido() throws Exception {
        String token = jwtService.gerarToken(new Usuario(UUID.randomUUID(), "maria", "11999999999", Perfil.GARCOM, "hash"));

        mockMvc.perform(get(ROTA_PROTEGIDA).header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }
}
