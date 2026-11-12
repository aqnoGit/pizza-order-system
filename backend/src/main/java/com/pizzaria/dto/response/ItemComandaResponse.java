package com.pizzaria.dto.response;

import java.math.BigDecimal;

public record ItemComandaResponse(String nome, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {
}
