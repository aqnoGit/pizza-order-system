package com.pizzaria.controller;

import com.pizzaria.dto.LoginFuncionarioRequest;
import com.pizzaria.dto.TokenResponse;
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
    public TokenResponse loginFuncionario(@Valid @RequestBody LoginFuncionarioRequest requisicao) {
        return authService.loginFuncionario(requisicao);
    }
}
