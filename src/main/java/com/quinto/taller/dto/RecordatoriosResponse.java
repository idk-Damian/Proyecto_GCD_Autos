package com.quinto.taller.dto;

import java.util.List;

public record RecordatoriosResponse(
        int intervaloKm,
        int intervaloMeses,
        int avisoKm,
        int avisoDias,
        List<RecordatorioDto> items
) {}
