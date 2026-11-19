package com.quinto.taller.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record OrdenRequest(
        @NotNull(message = "Debe seleccionar el vehículo")
        Integer autoId,

        Integer mecanicoId,

        LocalDate fechaIngreso,

        @Min(value = 0, message = "El kilometraje no puede ser negativo")
        Integer kilometraje,

        @NotBlank(message = "El tipo de mantenimiento es obligatorio")
        String tipoMantenimiento,

        @NotBlank(message = "El estado de la orden es obligatorio")
        String estado,

        @Size(max = 200, message = "El diagnóstico admite máximo 200 caracteres")
        String diagnostico,

        @Size(max = 200, message = "Los trabajos realizados admiten máximo 200 caracteres")
        String trabajosRealizados,

        /** Solo se usa cuando la orden no tiene líneas de detalle. */
        @DecimalMin(value = "0.00", message = "El costo no puede ser negativo")
        BigDecimal costo,

        List<@Valid DetalleRequest> detalles,

        /** Opcional: solicitud del portal que origina esta orden (se marca como atendida). */
        Integer solicitudId
) {}
