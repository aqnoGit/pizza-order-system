package com.pizzaria.dto.response;

import com.pizzaria.entity.StatusComanda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ComandaDetalhesResponse(UUID id, UUID mesaId, UUID garcomId, StatusComanda status,
        LocalDateTime abertaEm, LocalDateTime fechadaEm, BigDecimal valorTotal, List<ItemComandaResponse> itens) {
}
