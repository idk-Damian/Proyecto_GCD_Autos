package com.quinto.taller.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record DetalleRequest(
        /** Opcional: id del repuesto del inventario (descuenta stock). */
        Integer repuestoId,

        @NotBlank(message = "El tipo de la línea es obligatorio")
        String tipo,

        @NotBlank(message = "La descripción de la línea es obligatoria")
        @Size(max = 120, message = "La descripción admite máximo 120 caracteres")
        String descripcion,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "0.01", message = "La cantidad debe ser mayor a 0")
        BigDecimal cantidad,

        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "0.00", message = "El precio no puede ser negativo")
        BigDecimal precioUnitario
) {}
