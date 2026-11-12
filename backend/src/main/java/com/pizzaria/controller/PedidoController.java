package com.pizzaria.controller;

import com.pizzaria.dto.request.CriarPedidoRequest;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.service.PedidoService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comandas/{comandaId}/pedidos")
@RequiredArgsConstructor
public class PedidoController {

    private final PedidoService pedidoService;

    @PostMapping
    public ResponseEntity<PedidoResponse> criar(@PathVariable UUID comandaId,
            @Valid @RequestBody CriarPedidoRequest requisicao) {
        PedidoResponse pedido = pedidoService.criarPresencial(comandaId, requisicao);
        return ResponseEntity.created(URI.create("/pedidos/" + pedido.id())).body(pedido);
    }
}
