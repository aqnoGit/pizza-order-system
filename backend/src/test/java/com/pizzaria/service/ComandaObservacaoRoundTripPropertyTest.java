package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.PizzariaApplication;
import com.pizzaria.dto.response.ComandaResponse;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.lifecycle.AfterContainer;
import net.jqwik.api.lifecycle.BeforeContainer;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;

class ComandaObservacaoRoundTripPropertyTest {

    private static final int TAMANHO_MINIMO = 1;
    private static final int TAMANHO_MAXIMO = 500;
    private static final int PRIMEIRO_NUMERO_DE_MESA = 9500;

    private static final AtomicInteger PROXIMO_NUMERO_DE_MESA = new AtomicInteger(PRIMEIRO_NUMERO_DE_MESA);
    private static PostgreSQLContainer<?> postgres;
    private static ConfigurableApplicationContext contexto;

    @BeforeContainer
    static void subirContexto() {
        postgres = new PostgreSQLContainer<>("postgres:16").withDatabaseName("pizzaria_test");
        postgres.start();
        contexto = new SpringApplicationBuilder(PizzariaApplication.class)
                .web(WebApplicationType.SERVLET)
                .profiles("test")
                .run("--server.port=0",
                        "--spring.datasource.url=" + postgres.getJdbcUrl(),
                        "--spring.datasource.username=" + postgres.getUsername(),
                        "--spring.datasource.password=" + postgres.getPassword());
    }

    @AfterContainer
    static void derrubarContexto() {
        contexto.close();
        postgres.stop();
    }

    @Provide
    Arbitrary<String> observacoesValidas() {
        return Arbitraries.strings()
                .withCharRange(' ', '~')
                .withCharRange('\u00a1', '\u00ff')
                .ofMinLength(TAMANHO_MINIMO)
                .ofMaxLength(TAMANHO_MAXIMO);
    }

    @Property(tries = 50)
    void fecharComObservacaoEConsultarDevolveOMesmoTexto(@ForAll("observacoesValidas") String observacao) {
        JdbcTemplate jdbcTemplate = contexto.getBean(JdbcTemplate.class);
        ComandaService comandaService = contexto.getBean(ComandaService.class);
        UUID mesaId = UUID.randomUUID();
        UUID garcomId = UUID.randomUUID();
        UUID comandaId = UUID.randomUUID();
        jdbcTemplate.update("insert into mesas (id, numero, status) values (?, ?, 'OCUPADA')", mesaId,
                PROXIMO_NUMERO_DE_MESA.getAndIncrement());
        jdbcTemplate.update("insert into usuarios (id, nome, telefone, perfil) values (?, 'garcom jqwik', ?, 'GARCOM')",
                garcomId, "11" + Math.abs(garcomId.getMostSignificantBits() % 1_000_000_000L));
        jdbcTemplate.update("insert into comandas (id, mesa_id, garcom_id, status) values (?, ?, ?, 'ABERTA')",
                comandaId, mesaId, garcomId);
        try {
            ComandaResponse fechada = comandaService.fechar(comandaId, observacao);

            String observacaoConsultada = jdbcTemplate.queryForObject(
                    "select observacao from comandas where id = ?", String.class, comandaId);
            assertThat(fechada.observacao()).isEqualTo(observacao);
            assertThat(observacaoConsultada).isEqualTo(observacao);
        } finally {
            jdbcTemplate.update("delete from comandas where id = ?", comandaId);
            jdbcTemplate.update("delete from mesas where id = ?", mesaId);
            jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
        }
    }
}
