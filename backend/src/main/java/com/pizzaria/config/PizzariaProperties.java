package com.pizzaria.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pizzaria")
public record PizzariaProperties(String nome) {
}
