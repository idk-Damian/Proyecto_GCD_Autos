package com.quinto.taller.service;

import com.quinto.taller.dto.RecordatorioDto;
import com.quinto.taller.dto.RecordatoriosResponse;
import com.quinto.taller.model.Auto;
import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.model.Mantenimiento;
import com.quinto.taller.repository.AutoRepository;
import com.quinto.taller.repository.MantenimientoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Calcula el próximo mantenimiento preventivo de cada vehículo.
 * Regla: lo que ocurra primero entre "último preventivo + N km" y "último preventivo + M meses".
 */
@Service
public class RecordatorioService {

    public static final String VENCIDO = "VENCIDO";
    public static final String PROXIMO = "PROXIMO";
    public static final String AL_DIA = "AL_DIA";
    public static final String SIN_HISTORIAL = "SIN_HISTORIAL";

    private final AutoRepository autoRepository;
    private final MantenimientoRepository mantenimientoRepository;

    @Value("${app.mantenimiento.intervalo-km:5000}")
    private int intervaloKm;

    @Value("${app.mantenimiento.intervalo-meses:6}")
    private int intervaloMeses;

    @Value("${app.mantenimiento.aviso-km:1000}")
    private int avisoKm;

    @Value("${app.mantenimiento.aviso-dias:30}")
    private int avisoDias;

    public RecordatorioService(AutoRepository autoRepository, MantenimientoRepository mantenimientoRepository) {
        this.autoRepository = autoRepository;
        this.mantenimientoRepository = mantenimientoRepository;
    }

    public RecordatoriosResponse calcular() {
        LocalDate hoy = LocalDate.now();

        // Último preventivo finalizado de cada vehículo
        Map<Integer, Mantenimiento> ultimoPorAuto = mantenimientoRepository.findAll().stream()
                .filter(m -> m.getAuto() != null)
                .filter(m -> "Preventivo".equals(m.getTipoMantenimiento()))
                .filter(m -> Catalogos.ORDEN_FINALIZADO.equals(m.getEstado()))
                .collect(Collectors.toMap(m -> m.getAuto().getIdAuto(), m -> m,
                        (a, b) -> fechaServicio(a).isAfter(fechaServicio(b)) ? a : b));

        List<RecordatorioDto> items = new ArrayList<>();
        for (Auto auto : autoRepository.findAll()) {
            items.add(construir(auto, ultimoPorAuto.get(auto.getIdAuto()), hoy));
        }

        // Más urgentes primero
        List<String> prioridad = List.of(VENCIDO, PROXIMO, SIN_HISTORIAL, AL_DIA);
        items.sort(Comparator
                .comparing((RecordatorioDto r) -> prioridad.indexOf(r.estado()))
                .thenComparing(r -> r.kmRestantes() == null ? Integer.MAX_VALUE : r.kmRestantes()));

        return new RecordatoriosResponse(intervaloKm, intervaloMeses, avisoKm, avisoDias, items);
    }

    private RecordatorioDto construir(Auto auto, Mantenimiento ultimo, LocalDate hoy) {
        Cliente c = auto.getCliente();
        String cliente = c != null ? c.getNombre() + " " + c.getApellido() : null;
        String vehiculo = (Objects.toString(auto.getMarca(), "") + " " + Objects.toString(auto.getModelo(), "")).trim();
        Integer kmActual = auto.getKilometraje();

        if (ultimo == null) {
            return new RecordatorioDto(auto.getIdAuto(), auto.getPlaca(), vehiculo, cliente,
                    c != null ? c.getTelefono() : null, c != null ? c.getCorreo() : null,
                    kmActual, null, null, null, null, null, null, null, SIN_HISTORIAL);
        }

        LocalDate fechaUltimo = fechaServicio(ultimo);
        Integer kmUltimo = ultimo.getKilometraje();
        Integer proximoKm = kmUltimo != null ? kmUltimo + intervaloKm : null;
        LocalDate proximaFecha = fechaUltimo.plusMonths(intervaloMeses);
        Integer kmRestantes = (proximoKm != null && kmActual != null) ? proximoKm - kmActual : null;
        long diasRestantes = ChronoUnit.DAYS.between(hoy, proximaFecha);

        String estado;
        if ((kmRestantes != null && kmRestantes <= 0) || diasRestantes <= 0) {
            estado = VENCIDO;
        } else if ((kmRestantes != null && kmRestantes <= avisoKm) || diasRestantes <= avisoDias) {
            estado = PROXIMO;
        } else {
            estado = AL_DIA;
        }

        return new RecordatorioDto(auto.getIdAuto(), auto.getPlaca(), vehiculo, cliente,
                c != null ? c.getTelefono() : null, c != null ? c.getCorreo() : null,
                kmActual, ultimo.getIdMantenimiento(), fechaUltimo, kmUltimo, proximoKm, proximaFecha,
                kmRestantes, diasRestantes, estado);
    }

    private LocalDate fechaServicio(Mantenimiento m) {
        if (m.getFechaFinalizacion() != null) return m.getFechaFinalizacion();
        return m.getFechaIngreso() != null ? m.getFechaIngreso() : LocalDate.MIN;
    }
}
