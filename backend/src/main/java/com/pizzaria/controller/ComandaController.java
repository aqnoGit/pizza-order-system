package com.pizzaria.controller;

import com.pizzaria.dto.request.CriarComandaRequest;
import com.pizzaria.dto.response.ComandaDetalhesResponse;
import com.pizzaria.dto.response.ComandaResponse;
import com.pizzaria.service.ComandaService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comandas")
@RequiredArgsConstructor
public class ComandaController {

    private final ComandaService comandaService;

    @PostMapping
    public ResponseEntity<ComandaResponse> criar(@Valid @RequestBody CriarComandaRequest requisicao,
            Authentication autenticacao) {
        ComandaResponse comanda = comandaService.criar(requisicao, UUID.fromString(autenticacao.getName()));
        return ResponseEntity.created(URI.create("/comandas/" + comanda.id())).body(comanda);
    }

    @GetMapping("/aberta")
    public ComandaResponse buscarAbertaDaMesa(@RequestParam UUID mesaId) {
        return comandaService.buscarAbertaDaMesa(mesaId);
    }

    @GetMapping("/{id}")
    public ComandaDetalhesResponse buscarPorId(@PathVariable UUID id) {
        return comandaService.buscarPorId(id);
    }

    @PatchMapping("/{id}/fechar")
    public ComandaResponse fechar(@PathVariable UUID id) {
        return comandaService.fechar(id);
    }
}
