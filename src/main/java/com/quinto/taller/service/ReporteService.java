package com.quinto.taller.service;

import com.quinto.taller.dto.ReporteResumen;
import com.quinto.taller.dto.ReporteResumen.Fila;
import com.quinto.taller.dto.ReporteResumen.PuntoDia;
import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.DetalleOrden;
import com.quinto.taller.model.Mantenimiento;
import com.quinto.taller.repository.MantenimientoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;

/**
 * Reportes de gestión: ingresos, productividad por mecánico, servicios y repuestos más usados.
 * Una orden cuenta en el periodo según su fecha de finalización.
 */
@Service
public class ReporteService {

    private final MantenimientoRepository mantenimientoRepository;

    public ReporteService(MantenimientoRepository mantenimientoRepository) {
        this.mantenimientoRepository = mantenimientoRepository;
    }

    @Transactional(readOnly = true)
    public ReporteResumen resumen(LocalDate desde, LocalDate hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Debe indicar las fechas desde y hasta");
        }
        if (hasta.isBefore(desde)) {
            throw new IllegalArgumentException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }

        List<Mantenimiento> todas = mantenimientoRepository.findAll();
        int abiertas = (int) todas.stream().filter(m -> !Catalogos.ORDEN_FINALIZADO.equals(m.getEstado())).count();

        List<Mantenimiento> periodo = todas.stream()
                .filter(m -> Catalogos.ORDEN_FINALIZADO.equals(m.getEstado()))
                .filter(m -> {
                    LocalDate f = fechaCierre(m);
                    return f != null && !f.isBefore(desde) && !f.isAfter(hasta);
                })
                .toList();

        BigDecimal ingresos = sumar(periodo, Mantenimiento::getCosto);
        BigDecimal ticket = periodo.isEmpty() ? BigDecimal.ZERO
                : ingresos.divide(BigDecimal.valueOf(periodo.size()), 2, RoundingMode.HALF_UP);

        BigDecimal repuestos = BigDecimal.ZERO, manoObra = BigDecimal.ZERO, sinDetalle = BigDecimal.ZERO;
        Map<String, BigDecimal[]> servicios = new HashMap<>();
        Map<String, BigDecimal[]> partes = new HashMap<>();

        for (Mantenimiento m : periodo) {
            if (m.getDetalles().isEmpty()) {
                sinDetalle = sinDetalle.add(nz(m.getCosto()));
                continue;
            }
            for (DetalleOrden d : m.getDetalles()) {
                BigDecimal sub = d.getSubtotal();
                String clave = d.getRepuesto() != null ? d.getRepuesto().getNombre() : capitalizar(d.getDescripcion());
                if (Catalogos.DETALLE_REPUESTO.equals(d.getTipo())) {
                    repuestos = repuestos.add(sub);
                    acumular(partes, clave, d.getCantidad(), sub);
                } else {
                    manoObra = manoObra.add(sub);
                    acumular(servicios, clave, BigDecimal.ONE, sub);
                }
            }
        }

        // Por mecánico
        Map<String, BigDecimal[]> mecanicos = new HashMap<>();
        for (Mantenimiento m : periodo) {
            String nombre = m.getMecanico() == null ? "Sin asignar"
                    : Objects.requireNonNullElse(m.getMecanico().getNombreCompleto(), m.getMecanico().getUsername());
            acumular(mecanicos, nombre, BigDecimal.ONE, nz(m.getCosto()));
        }

        // Por tipo
        Map<String, BigDecimal[]> tipos = new HashMap<>();
        for (Mantenimiento m : periodo) {
            acumular(tipos, Objects.requireNonNullElse(m.getTipoMantenimiento(), "Sin tipo"), BigDecimal.ONE, nz(m.getCosto()));
        }

        // Ingresos por día (para el gráfico)
        TreeMap<LocalDate, BigDecimal> porDia = new TreeMap<>();
        for (Mantenimiento m : periodo) {
            porDia.merge(fechaCierre(m), nz(m.getCosto()), BigDecimal::add);
        }
        List<PuntoDia> dias = porDia.entrySet().stream().map(e -> new PuntoDia(e.getKey(), e.getValue())).toList();

        long vehiculos = periodo.stream().map(m -> m.getAuto().getIdAuto()).distinct().count();

        return new ReporteResumen(desde, hasta, periodo.size(), abiertas, (int) vehiculos,
                ingresos, ticket, repuestos, manoObra, sinDetalle,
                aFilas(mecanicos, 50), aFilas(tipos, 10), aFilas(servicios, 10), aFilas(partes, 10), dias);
    }

    private LocalDate fechaCierre(Mantenimiento m) {
        return m.getFechaFinalizacion() != null ? m.getFechaFinalizacion() : m.getFechaIngreso();
    }

    private void acumular(Map<String, BigDecimal[]> mapa, String clave, BigDecimal cantidad, BigDecimal total) {
        BigDecimal[] v = mapa.computeIfAbsent(clave, k -> new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
        v[0] = v[0].add(nz(cantidad));
        v[1] = v[1].add(nz(total));
    }

    private List<Fila> aFilas(Map<String, BigDecimal[]> mapa, int limite) {
        return mapa.entrySet().stream()
                .map(e -> new Fila(e.getKey(), e.getValue()[0], e.getValue()[1].setScale(2, RoundingMode.HALF_UP)))
                .sorted(Comparator.comparing(Fila::total).reversed())
                .limit(limite)
                .toList();
    }

    private BigDecimal sumar(List<Mantenimiento> lista, Function<Mantenimiento, BigDecimal> f) {
        return lista.stream().map(f).map(this::nz).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    private String capitalizar(String s) {
        if (s == null || s.isBlank()) return "Sin descripción";
        String t = s.trim().toLowerCase(Locale.ROOT);
        return Character.toUpperCase(t.charAt(0)) + t.substring(1);
    }
}
