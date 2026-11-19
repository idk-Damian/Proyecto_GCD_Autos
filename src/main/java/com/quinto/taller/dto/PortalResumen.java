package com.quinto.taller.dto;

import com.quinto.taller.model.Auto;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.model.Mantenimiento;
import com.quinto.taller.model.SolicitudServicio;

import java.util.List;

/** Todo lo que el cliente ve en su portal, en una sola llamada. */
public record PortalResumen(
        String username,
        Cliente cliente,
        List<Auto> vehiculos,
        List<Mantenimiento> ordenes,
        List<RecordatorioDto> recordatorios,
        List<SolicitudServicio> solicitudes,
        int intervaloKm,
        int intervaloMeses
) {}
