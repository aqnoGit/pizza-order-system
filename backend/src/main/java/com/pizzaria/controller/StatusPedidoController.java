package com.pizzaria.controller;

import com.pizzaria.dto.request.AtualizarStatusPedidoRequest;
import com.pizzaria.dto.response.PedidoResponse;
import com.pizzaria.service.PedidoService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/{id}/status")
@RequiredArgsConstructor
public class StatusPedidoController {

    private final PedidoService pedidoService;

    @PatchMapping
    public PedidoResponse atualizar(@PathVariable UUID id, @Valid @RequestBody AtualizarStatusPedidoRequest requisicao) {
        return pedidoService.atualizarStatus(id, requisicao.status());
    }
}
