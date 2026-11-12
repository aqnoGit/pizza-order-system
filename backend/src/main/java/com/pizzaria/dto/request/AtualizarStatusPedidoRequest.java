package com.pizzaria.dto.request;

import com.pizzaria.entity.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AtualizarStatusPedidoRequest(@NotNull StatusPedido status) {
}
