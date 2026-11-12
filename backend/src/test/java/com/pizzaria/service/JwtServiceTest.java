package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.config.JwtProperties;
import com.pizzaria.entity.Perfil;
import com.pizzaria.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SEGREDO = "segredo-de-teste-com-mais-de-32-caracteres-ok";

    private final JwtService jwtService = new JwtService(new JwtProperties(SEGREDO, 60));

    @Test
    void tokenContemUserIdPerfilEExpiracao() {
        UUID id = UUID.randomUUID();
        Usuario cozinheiro = new Usuario(id, "joao", "11999999999", Perfil.COZINHA, "hash");

        Claims claims = jwtService.lerClaims(jwtService.gerarToken(cozinheiro));

        assertThat(claims.get("userId", String.class)).isEqualTo(id.toString());
        assertThat(claims.get("perfil", String.class)).isEqualTo("COZINHA");
        assertThat(claims.getExpiration()).isAfter(claims.getIssuedAt());
    }

    @Test
    void rejeitaTokenAssinadoComOutroSegredo() {
        JwtService outro = new JwtService(new JwtProperties("outro-segredo-com-mais-de-32-caracteres-123", 60));
        Usuario garcom = new Usuario(UUID.randomUUID(), "maria", "11999999999", Perfil.GARCOM, "hash");

        String token = outro.gerarToken(garcom);

        assertThatThrownBy(() -> jwtService.lerClaims(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejeitaTokenExpirado() {
        JwtService expirado = new JwtService(new JwtProperties(SEGREDO, -1));
        Usuario garcom = new Usuario(UUID.randomUUID(), "maria", "11999999999", Perfil.GARCOM, "hash");

        String token = expirado.gerarToken(garcom);

        assertThatThrownBy(() -> jwtService.lerClaims(token)).isInstanceOf(JwtException.class);
    }
}
