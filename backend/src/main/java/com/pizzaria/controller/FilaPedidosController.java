package com.pizzaria.controller;

import com.pizzaria.dto.response.PedidoFilaResponse;
import com.pizzaria.service.FilaPedidosService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pedidos/fila")
@RequiredArgsConstructor
public class FilaPedidosController {

    private final FilaPedidosService filaPedidosService;

    @GetMapping
    public List<PedidoFilaResponse> listarFila() {
        return filaPedidosService.listarFila();
    }
}
