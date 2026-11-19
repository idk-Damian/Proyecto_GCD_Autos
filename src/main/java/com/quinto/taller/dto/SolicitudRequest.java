package com.quinto.taller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record SolicitudRequest(
        @NotNull(message = "Seleccione el vehículo")
        Integer autoId,

        @NotNull(message = "Seleccione la fecha preferida")
        LocalDate fechaPreferida,

        @NotBlank(message = "Seleccione la franja horaria")
        String franja,

        @NotBlank(message = "Seleccione el tipo de servicio")
        String tipoMantenimiento,

        @NotBlank(message = "Describa el servicio que necesita")
        @Size(max = 300, message = "La descripción admite máximo 300 caracteres")
        String descripcion
) {}
