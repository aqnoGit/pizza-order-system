package com.pizzaria.controller;

import com.pizzaria.dto.response.MesaResponse;
import com.pizzaria.service.MesaService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesas")
@RequiredArgsConstructor
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    public List<MesaResponse> listar() {
        return mesaService.listar();
    }
}
