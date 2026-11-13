package com.pizzaria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "impressora")
public record ImpressoraProperties(String host, int porta, int timeoutConexaoMilissegundos) {
}
