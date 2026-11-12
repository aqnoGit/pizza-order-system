package com.pizzaria.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComprovanteResponse(String nomePizzaria, Integer numeroMesa, List<ItemComprovanteResponse> itens,
        BigDecimal valorTotal, LocalDateTime emitidoEm) {
}
