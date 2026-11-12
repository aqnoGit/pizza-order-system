package com.pizzaria.exception;

import com.pizzaria.dto.response.ErroResponse;
import java.time.Instant;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String MENSAGEM_DADOS_INVALIDOS = "Dados invalidos";

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ErroResponse> tratarCredenciaisInvalidas(CredenciaisInvalidasException excecao) {
        return resposta(HttpStatus.UNAUTHORIZED, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResponse> tratarNaoEncontrado(RecursoNaoEncontradoException excecao) {
        return resposta(HttpStatus.NOT_FOUND, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResponse> tratarConflito(ConflitoException excecao) {
        return resposta(HttpStatus.CONFLICT, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(ComandaFechadaException.class)
    public ResponseEntity<ErroResponse> tratarComandaFechada(ComandaFechadaException excecao) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(ObservacaoInvalidaException.class)
    public ResponseEntity<ErroResponse> tratarObservacaoInvalida(ObservacaoInvalidaException excecao) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(ProdutoIndisponivelException.class)
    public ResponseEntity<ErroResponse> tratarProdutoIndisponivel(ProdutoIndisponivelException excecao) {
        return resposta(HttpStatus.UNPROCESSABLE_ENTITY, excecao.getMessage(), List.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResponse> tratarValidacao(MethodArgumentNotValidException excecao) {
        List<String> campos = excecao.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField())
                .toList();
        return resposta(HttpStatus.BAD_REQUEST, MENSAGEM_DADOS_INVALIDOS, campos);
    }

    private ResponseEntity<ErroResponse> resposta(HttpStatus status, String mensagem, List<String> campos) {
        ErroResponse corpo = new ErroResponse(Instant.now(), status.value(), mensagem, campos);
        return ResponseEntity.status(status).body(corpo);
    }
}
