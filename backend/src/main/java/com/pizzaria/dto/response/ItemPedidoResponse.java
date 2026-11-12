package com.pizzaria.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemPedidoResponse(UUID produtoId, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
}
