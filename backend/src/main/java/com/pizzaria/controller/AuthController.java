package com.pizzaria.controller;

import com.pizzaria.dto.request.LoginClienteRequest;
import com.pizzaria.dto.request.LoginFuncionarioRequest;
import com.pizzaria.dto.response.TokenResponse;
import com.pizzaria.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/funcionario")
    public TokenResponse loginFuncionario(@Valid @RequestBody LoginFuncionarioRequest request) {
        return authService.loginFuncionario(request);
    }

    @PostMapping("/cliente")
    public TokenResponse loginCliente(@Valid @RequestBody LoginClienteRequest request) {
        return authService.loginCliente(request);
    }
}
