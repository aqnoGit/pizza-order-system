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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, SecurityConfigTest.JwtServiceReal.class, SecurityConfigTest.RotasPorPerfil.class})
@ActiveProfiles("test")
class SecurityConfigTest {

    private static final String ROTA_PROTEGIDA = "/rota-protegida";

    @RestController
    static class RotasPorPerfil {

        @GetMapping("/teste/cozinha")
        @PreAuthorize("hasRole('COZINHA')")
        String rotaDaCozinha() {
            return "cozinha";
        }

        @GetMapping("/teste/cliente")
        @PreAuthorize("hasRole('CLIENTE')")
        String rotaDoCliente() {
            return "cliente";
        }

        @GetMapping("/teste/garcom")
        @PreAuthorize("hasRole('GARCOM')")
        String rotaDoGarcom() {
            return "garcom";
        }
    }

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

    @Test
    void retorna401EmRotaPorPerfilSemToken() throws Exception {
        mockMvc.perform(get("/teste/cozinha")).andExpect(status().isUnauthorized());
    }

    @Test
    void retorna403QuandoGarcomAcessaRotaDaCozinha() throws Exception {
        mockMvc.perform(get("/teste/cozinha").header("Authorization", "Bearer " + tokenDe(Perfil.GARCOM)))
                .andExpect(status().isForbidden());
    }

    @Test
    void retorna403QuandoClienteAcessaRotaDoGarcom() throws Exception {
        mockMvc.perform(get("/teste/garcom").header("Authorization", "Bearer " + tokenDe(Perfil.CLIENTE)))
                .andExpect(status().isForbidden());
    }

    @Test
    void permiteQuandoCozinhaAcessaRotaDaCozinha() throws Exception {
        mockMvc.perform(get("/teste/cozinha").header("Authorization", "Bearer " + tokenDe(Perfil.COZINHA)))
                .andExpect(status().isOk());
    }

    @Test
    void adminAcessaRotasDeGarcomCozinhaECliente() throws Exception {
        String cabecalho = "Bearer " + tokenDe(Perfil.ADMIN);

        mockMvc.perform(get("/teste/garcom").header("Authorization", cabecalho)).andExpect(status().isOk());
        mockMvc.perform(get("/teste/cozinha").header("Authorization", cabecalho)).andExpect(status().isOk());
        mockMvc.perform(get("/teste/cliente").header("Authorization", cabecalho)).andExpect(status().isOk());
    }

    @Test
    void garcomNaoAcessaRotaDeCliente() throws Exception {
        mockMvc.perform(get("/teste/cliente").header("Authorization", "Bearer " + tokenDe(Perfil.GARCOM)))
                .andExpect(status().isForbidden());
    }

    private String tokenDe(Perfil perfil) {
        return jwtService.gerarToken(new Usuario(UUID.randomUUID(), "usuario", "11999999999", perfil, "hash"));
    }
}
