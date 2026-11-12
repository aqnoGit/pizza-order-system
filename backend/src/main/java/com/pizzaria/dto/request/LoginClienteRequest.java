package com.pizzaria.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LoginClienteRequest(@NotBlank String telefone, @NotBlank String senha) {
}
