package com.pizzaria.dto.response;

import com.pizzaria.entity.StatusMesa;
import java.util.UUID;

public record MesaResponse(UUID id, Integer numero, StatusMesa status) {
}
