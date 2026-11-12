package com.pizzaria.dto;

import jakarta.validation.constraints.NotBlank;

public record LoginFuncionarioRequest(@NotBlank String usuario, @NotBlank String senha) {
}
