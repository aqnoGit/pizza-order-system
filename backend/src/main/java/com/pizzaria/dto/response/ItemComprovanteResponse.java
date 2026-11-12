package com.pizzaria.dto.response;

import java.math.BigDecimal;

public record ItemComprovanteResponse(String nome, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
}
