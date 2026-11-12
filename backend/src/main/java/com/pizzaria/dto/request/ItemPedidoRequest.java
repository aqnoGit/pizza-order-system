package com.pizzaria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record ItemPedidoRequest(@NotNull UUID produtoId, @Min(1) int quantidade) {
}
