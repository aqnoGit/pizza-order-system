package com.pizzaria.dto.response;

import java.util.UUID;

public record PedidoProntoResponse(UUID pedidoId, Integer numeroMesa) {
}
