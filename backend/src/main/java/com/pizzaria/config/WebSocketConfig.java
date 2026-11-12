package com.pizzaria.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    public static final String ENDPOINT = "/ws";
    public static final String PREFIXO_BROKER = "/topic";
    public static final String PREFIXO_APLICACAO = "/app";

    private final String[] origensPermitidas;

    public WebSocketConfig(
            @Value("${cors.origens-permitidas:https://pizza-order-system.pages.dev,http://localhost:4200,http://localhost:4201}") String[] origensPermitidas) {
        this.origensPermitidas = origensPermitidas;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registro) {
        registro.enableSimpleBroker(PREFIXO_BROKER);
        registro.setApplicationDestinationPrefixes(PREFIXO_APLICACAO);
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registro) {
        registro.addEndpoint(ENDPOINT)
                .setAllowedOriginPatterns(origensPermitidas)
                .withSockJS();
    }
}
