package com.pizzaria.dto.response;

import com.pizzaria.entity.StatusComanda;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ComandaResponse(UUID id, UUID mesaId, UUID garcomId, StatusComanda status, LocalDateTime abertaEm,
        LocalDateTime fechadaEm, BigDecimal valorTotal) {
}
