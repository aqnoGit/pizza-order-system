package com.pizzaria.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record ItemPedidoRequest(@NotNull UUID produtoId, @Min(1) int quantidade,
        @Size(max = 255) String observacao) {
}
