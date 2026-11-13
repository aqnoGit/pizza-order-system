package com.pizzaria.exception;

public class ImpressoraIndisponivelException extends RuntimeException {

    public ImpressoraIndisponivelException(String mensagem, Throwable causa) {
        super(mensagem, causa);
    }
}
