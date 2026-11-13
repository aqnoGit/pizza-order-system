package com.pizzaria.controller;

import com.pizzaria.service.ComprovanteService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comprovantes")
@RequiredArgsConstructor
public class ComprovanteController {

    private final ComprovanteService comprovanteService;

    @PostMapping("/{comandaId}/imprimir")
    public ResponseEntity<Void> imprimir(@PathVariable UUID comandaId) {
        comprovanteService.imprimir(comandaId);
        return ResponseEntity.ok().build();
    }
}
