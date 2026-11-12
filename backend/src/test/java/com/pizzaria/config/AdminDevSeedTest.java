package com.pizzaria.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.PostgresIntegrationTest;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

class AdminDevSeedTest {

    private static final String BUSCAR_ADMIN = "select * from usuarios where nome = 'admin'";

    @Nested
    @ActiveProfiles({"test", "dev"})
    class ComPerfilDev extends PostgresIntegrationTest {

        @Autowired
        private JdbcTemplate jdbcTemplate;

        @Autowired
        private PasswordEncoder passwordEncoder;

        @Test
        void criaUsuarioAdminComSenhaEmBcrypt() {
            Map<String, Object> admin = jdbcTemplate.queryForMap(BUSCAR_ADMIN);

            assertThat(admin.get("perfil")).isEqualTo("ADMIN");
            assertThat(admin.get("credenciais")).isNotEqualTo("admin123");
            assertThat(passwordEncoder.matches("admin123", (String) admin.get("credenciais"))).isTrue();
        }
    }

    @Nested
    class SemPerfilDev extends PostgresIntegrationTest {

        @Autowired
        private ApplicationContext contexto;

        @Test
        void naoRegistraOSeed() {
            assertThat(contexto.getBeansOfType(AdminDevSeed.class)).isEmpty();
        }
    }
}
