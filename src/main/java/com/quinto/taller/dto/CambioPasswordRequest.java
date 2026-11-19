package com.quinto.taller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambioPasswordRequest(
        @NotBlank(message = "Ingrese su contraseña actual")
        String actual,

        @NotBlank(message = "Ingrese la nueva contraseña")
        @Size(min = 6, max = 60, message = "La nueva contraseña debe tener al menos 6 caracteres")
        String nueva
) {}
