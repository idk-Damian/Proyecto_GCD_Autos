package com.quinto.taller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Resumen de gestión del taller para un periodo. */
public record ReporteResumen(
        LocalDate desde,
        LocalDate hasta,
        int ordenesFinalizadas,
        int ordenesAbiertas,
        int vehiculosAtendidos,
        BigDecimal ingresos,
        BigDecimal ticketPromedio,
        BigDecimal totalRepuestos,
        BigDecimal totalManoObra,
        BigDecimal totalSinDetalle,
        List<Fila> porMecanico,
        List<Fila> porTipo,
        List<Fila> topServicios,
        List<Fila> topRepuestos,
        List<PuntoDia> ingresosPorDia
) {
    /** Fila genérica: etiqueta, cantidad (órdenes o unidades) y monto. */
    public record Fila(String etiqueta, BigDecimal cantidad, BigDecimal total) {}

    public record PuntoDia(LocalDate fecha, BigDecimal total) {}
}
