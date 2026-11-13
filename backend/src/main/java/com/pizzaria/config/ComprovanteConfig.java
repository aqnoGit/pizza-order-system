package com.pizzaria.config;

import java.time.Clock;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({PizzariaProperties.class, ImpressoraProperties.class})
public class ComprovanteConfig {

    @Bean
    public Clock relogio() {
        return Clock.systemDefaultZone();
    }
}
