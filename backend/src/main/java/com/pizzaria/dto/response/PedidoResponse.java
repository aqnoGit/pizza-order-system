package com.pizzaria.dto.response;

import com.pizzaria.entity.OrigemPedido;
import com.pizzaria.entity.StatusPedido;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PedidoResponse(UUID id, UUID comandaId, OrigemPedido origem, StatusPedido status,
        LocalDateTime registradoEm, BigDecimal valorTotal, List<ItemPedidoResponse> itens) {
}
