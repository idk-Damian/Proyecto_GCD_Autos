package com.quinto.taller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PerfilRequest(
        @Pattern(regexp = "^$|^\\d{7,10}$", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 50, message = "El correo admite máximo 50 caracteres")
        String correo
) {}
