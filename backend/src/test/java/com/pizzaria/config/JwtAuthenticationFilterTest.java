package com.pizzaria.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import com.pizzaria.service.JwtService;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class JwtAuthenticationFilterTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-ok";

    private final JwtService jwtService = new JwtService(new JwtProperties(SEGREDO, 60));
    private final JwtAuthenticationFilter filtro = new JwtAuthenticationFilter(jwtService);

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void autenticaComRolePorPerfilQuandoTokenValido() throws Exception {
        UUID id = UUID.randomUUID();
        String token = jwtService.gerarToken(new Usuario(id, "maria", "11999999999", Perfil.GARCOM, "hash"));

        Authentication autenticacao = executar("Bearer " + token);

        assertThat(autenticacao.getName()).isEqualTo(id.toString());
        assertThat(autenticacao.getAuthorities()).extracting("authority").containsExactly("ROLE_GARCOM");
    }

    @Test
    void naoAutenticaQuandoSemCabecalho() throws Exception {
        assertThat(executar(null)).isNull();
    }

    @Test
    void naoAutenticaQuandoCabecalhoNaoEhBearer() throws Exception {
        assertThat(executar("Basic abc")).isNull();
    }

    @Test
    void naoAutenticaQuandoTokenInvalido() throws Exception {
        assertThat(executar("Bearer token-invalido")).isNull();
    }

    @Test
    void naoAutenticaQuandoTokenAssinadoComOutroSegredo() throws Exception {
        JwtService outro = new JwtService(new JwtProperties("outro-segredo-com-mais-de-32-caracteres-123", 60));
        String token = outro.gerarToken(new Usuario(UUID.randomUUID(), "joao", "11999999999", Perfil.COZINHA, "hash"));

        assertThat(executar("Bearer " + token)).isNull();
    }

    @Test
    void continuaACadeiaMesmoSemAutenticar() throws Exception {
        MockFilterChain cadeia = new MockFilterChain();
        MockHttpServletRequest requisicao = new MockHttpServletRequest();

        filtro.doFilter(requisicao, new MockHttpServletResponse(), cadeia);

        assertThat(cadeia.getRequest()).isSameAs(requisicao);
    }

    private Authentication executar(String cabecalho) throws Exception {
        MockHttpServletRequest requisicao = new MockHttpServletRequest();
        if (cabecalho != null) {
            requisicao.addHeader("Authorization", cabecalho);
        }
        filtro.doFilter(requisicao, new MockHttpServletResponse(), new MockFilterChain());
        return SecurityContextHolder.getContext().getAuthentication();
    }
}
