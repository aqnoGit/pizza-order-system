package com.pizzaria.exception;

import static org.assertj.core.api.Assertions.assertThat;

import com.pizzaria.dto.response.ErroResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void deveResponder422QuandoComandaEstaFechada() {
        ResponseEntity<ErroResponse> resposta = handler
                .tratarComandaFechada(new ComandaFechadaException("Comanda fechada"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody().status()).isEqualTo(422);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Comanda fechada");
    }

    @Test
    void deveResponder422QuandoProdutoEstaIndisponivel() {
        ResponseEntity<ErroResponse> resposta = handler
                .tratarProdutoIndisponivel(new ProdutoIndisponivelException("Produto indisponivel"));

        assertThat(resposta.getStatusCode()).isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY);
        assertThat(resposta.getBody().mensagem()).isEqualTo("Produto indisponivel");
    }
}
