package com.quinto.taller.dto;

import java.time.LocalDate;

/**
 * Próximo mantenimiento preventivo calculado para un vehículo.
 * estado: VENCIDO, PROXIMO, AL_DIA o SIN_HISTORIAL
 */
public record RecordatorioDto(
        Integer idAuto,
        String placa,
        String vehiculo,
        String cliente,
        String telefono,
        String correo,
        Integer kmActual,
        Integer idUltimaOrden,
        LocalDate ultimoServicio,
        Integer kmUltimoServicio,
        Integer proximoKm,
        LocalDate proximaFecha,
        Integer kmRestantes,
        Long diasRestantes,
        String estado
) {}
