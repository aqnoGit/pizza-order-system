package com.pizzaria.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CriarComandaRequest(@NotNull UUID mesaId) {
}
