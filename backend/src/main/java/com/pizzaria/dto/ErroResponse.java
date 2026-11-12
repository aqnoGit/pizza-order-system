package com.pizzaria.dto;

import java.time.Instant;
import java.util.List;

public record ErroResponse(Instant timestamp, int status, String mensagem, List<String> campos) {
}
