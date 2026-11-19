package com.quinto.taller.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
        @NotBlank(message = "La cédula es obligatoria")
        @Pattern(regexp = "\\d{10}", message = "La cédula debe tener 10 dígitos")
        String cedula,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 50, message = "El nombre admite máximo 50 caracteres")
        String nombre,

        @NotBlank(message = "El apellido es obligatorio")
        @Size(max = 50, message = "El apellido admite máximo 50 caracteres")
        String apellido,

        @Pattern(regexp = "^$|^\\d{7,10}$", message = "El teléfono debe tener entre 7 y 10 dígitos")
        String telefono,

        @Email(message = "El correo no tiene un formato válido")
        @Size(max = 50, message = "El correo admite máximo 50 caracteres")
        String correo
) {}
