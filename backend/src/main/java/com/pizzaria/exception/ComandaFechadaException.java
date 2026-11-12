package com.pizzaria.exception;

public class ComandaFechadaException extends RuntimeException {

    public ComandaFechadaException(String mensagem) {
        super(mensagem);
    }
}
