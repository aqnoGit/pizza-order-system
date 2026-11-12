package com.pizzaria.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pizzaria.PizzariaApplication;
import com.pizzaria.exception.ObservacaoInvalidaException;
import java.math.BigDecimal;
import java.util.Map;
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

class ComandaObservacaoLimitePropertyTest {

    private static final int TAMANHO_MINIMO_INVALIDO = 501;
    private static final int TAMANHO_MAXIMO_GERADO = 2000;
    private static final int PRIMEIRO_NUMERO_DE_MESA = 9700;

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
    Arbitrary<String> observacoesMuitoLongas() {
        return Arbitraries.strings()
                .withCharRange(' ', '~')
                .withCharRange('¡', 'ÿ')
                .ofMinLength(TAMANHO_MINIMO_INVALIDO)
                .ofMaxLength(TAMANHO_MAXIMO_GERADO);
    }

    @Property(tries = 50)
    void fecharComObservacaoMaiorQueOLimiteRejeitaEMantemComandaAberta(
            @ForAll("observacoesMuitoLongas") String observacao) {
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
            assertThatThrownBy(() -> comandaService.fechar(comandaId, observacao))
                    .isInstanceOf(ObservacaoInvalidaException.class);

            Map<String, Object> comanda = jdbcTemplate.queryForMap(
                    "select status, observacao, valor_total from comandas where id = ?", comandaId);
            assertThat(comanda.get("status")).isEqualTo("ABERTA");
            assertThat(comanda.get("observacao")).isNull();
            assertThat((BigDecimal) comanda.get("valor_total")).isEqualByComparingTo(BigDecimal.ZERO);
        } finally {
            jdbcTemplate.update("delete from comandas where id = ?", comandaId);
            jdbcTemplate.update("delete from mesas where id = ?", mesaId);
            jdbcTemplate.update("delete from usuarios where id = ?", garcomId);
        }
    }
}
