package com.pizzaria.exception;

public class CredenciaisInvalidasException extends RuntimeException {

    public static final String MENSAGEM = "Credenciais incorretas";

    public CredenciaisInvalidasException() {
        super(MENSAGEM);
    }
}
