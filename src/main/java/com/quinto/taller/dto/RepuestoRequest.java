package com.quinto.taller.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public record RepuestoRequest(
        @NotBlank(message = "El código es obligatorio")
        @Pattern(regexp = "^[A-Za-z0-9-]{2,20}$", message = "El código admite 2-20 letras, números o guiones")
        String codigo,

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre admite máximo 100 caracteres")
        String nombre,

        @NotBlank(message = "La unidad es obligatoria")
        @Size(max = 15, message = "La unidad admite máximo 15 caracteres")
        String unidad,

        /** Solo se usa al crear; después el stock se mueve con entradas y órdenes. */
        @DecimalMin(value = "0.00", message = "El stock no puede ser negativo")
        BigDecimal stock,

        @NotNull(message = "El stock mínimo es obligatorio")
        @DecimalMin(value = "0.00", message = "El stock mínimo no puede ser negativo")
        BigDecimal stockMinimo,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal precioVenta
) {}
