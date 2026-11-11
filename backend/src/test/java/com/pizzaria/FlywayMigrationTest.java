package com.pizzaria;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class FlywayMigrationTest extends PostgresIntegrationTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void flywayEstaConfiguradoParaLerMigrationsDoClasspath() {
        assertThat(flyway.getConfiguration().getLocations())
                .anyMatch(location -> location.getPath().equals("db/migration"));
    }

    @Test
    void flywayCriaTabelaDeHistoricoNoPostgres() {
        Integer tabelas = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_name = 'flyway_schema_history'",
                Integer.class);

        assertThat(tabelas).isEqualTo(1);
    }

    @Test
    void hibernateNaoCriaTabelasSozinho() {
        Integer tabelasDeNegocio = jdbcTemplate.queryForObject(
                "select count(*) from information_schema.tables where table_schema = 'public' and table_name <> 'flyway_schema_history'",
                Integer.class);

        assertThat(tabelasDeNegocio).isZero();
    }
}
