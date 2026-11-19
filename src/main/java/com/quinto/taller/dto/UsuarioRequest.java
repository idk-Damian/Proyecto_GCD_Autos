package com.quinto.taller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank(message = "El usuario es obligatorio")
        @Pattern(regexp = "^[a-zA-Z0-9._]{3,30}$", message = "El usuario debe tener 3-30 caracteres (letras, números, punto o guion bajo)")
        String username,

        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 6, max = 60, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        @NotBlank(message = "El rol es obligatorio")
        String rol,

        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 80, message = "El nombre admite máximo 80 caracteres")
        String nombreCompleto
) {}
