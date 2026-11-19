package com.quinto.taller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Vehículo que el cliente registra por su cuenta desde el portal. */
public record VehiculoPortalRequest(
        @NotBlank(message = "La placa es obligatoria")
        String placa,

        @NotBlank(message = "La marca es obligatoria")
        @Size(max = 30, message = "La marca admite máximo 30 caracteres")
        String marca,

        @NotBlank(message = "El modelo es obligatorio")
        @Size(max = 30, message = "El modelo admite máximo 30 caracteres")
        String modelo,

        @Min(value = 1950, message = "El año debe ser mayor o igual a 1950")
        @Max(value = 2030, message = "El año no es válido")
        Integer anio,

        @Size(max = 20, message = "El color admite máximo 20 caracteres")
        String color,

        @Min(value = 0, message = "El kilometraje no puede ser negativo")
        Integer kilometraje
) {}
