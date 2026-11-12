package com.pizzaria.dto.response;

import com.pizzaria.entity.OrigemPedido;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoFilaResponse(UUID id, OrigemPedido origem, Integer numeroMesa, LocalDateTime registradoEm,
        List<ItemFilaResponse> itens) {
}
