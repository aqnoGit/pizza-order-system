package com.pizzaria.dto.response;

import java.util.UUID;

public record ItemFilaResponse(UUID produtoId, String nomeProduto, int quantidade, String observacao) {
}
