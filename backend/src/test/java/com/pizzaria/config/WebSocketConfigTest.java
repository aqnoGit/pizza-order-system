package com.pizzaria.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.PostgresIntegrationTest;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class WebSocketConfigTest extends PostgresIntegrationTest {

    private static final String DESTINO = "/topic/teste";
    private static final String MENSAGEM = "pedido pronto";
    private static final int SEGUNDOS_ESPERA = 5;
    private static final String ORIGEM_FRONTEND = "http://localhost:4200";

    @LocalServerPort
    private int porta;

    @Autowired
    private SimpMessagingTemplate template;

    @Test
    void clienteSockJsConectaEmWsERecebeMensagemDoBroker() throws Exception {
        BlockingQueue<String> recebidas = new LinkedBlockingQueue<>();
        StompSession sessao = conectar();

        sessao.subscribe(DESTINO, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return String.class;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                recebidas.add((String) payload);
            }
        });
        aguardarInscricao();
        template.convertAndSend(DESTINO, MENSAGEM);

        assertThat(recebidas.poll(SEGUNDOS_ESPERA, TimeUnit.SECONDS)).isEqualTo(MENSAGEM);
        sessao.disconnect();
    }

    private StompSession conectar() throws Exception {
        SockJsClient sockJs = new SockJsClient(List.of(new WebSocketTransport(new StandardWebSocketClient())));
        WebSocketStompClient cliente = new WebSocketStompClient(sockJs);
        cliente.setMessageConverter(new StringMessageConverter());
        WebSocketHttpHeaders cabecalhos = new WebSocketHttpHeaders();
        cabecalhos.setOrigin(ORIGEM_FRONTEND);
        return cliente.connectAsync("http://localhost:" + porta + WebSocketConfig.ENDPOINT, cabecalhos,
                new StompSessionHandlerAdapter() {
                }).get(SEGUNDOS_ESPERA, TimeUnit.SECONDS);
    }

    private void aguardarInscricao() throws InterruptedException {
        TimeUnit.MILLISECONDS.sleep(500);
    }
}
