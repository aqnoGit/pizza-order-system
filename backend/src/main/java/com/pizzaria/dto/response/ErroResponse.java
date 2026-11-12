package com.pizzaria.dto.response;

import java.time.Instant;
import java.util.List;

public record ErroResponse(Instant timestamp, int status, String mensagem, List<String> campos) {
}
