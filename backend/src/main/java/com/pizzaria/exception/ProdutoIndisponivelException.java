package com.pizzaria.exception;

public class ProdutoIndisponivelException extends RuntimeException {

    public ProdutoIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
