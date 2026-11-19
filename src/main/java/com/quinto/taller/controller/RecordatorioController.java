package com.quinto.taller.controller;

import com.quinto.taller.dto.RecordatoriosResponse;
import com.quinto.taller.service.RecordatorioService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recordatorios")
public class RecordatorioController {

    private final RecordatorioService recordatorioService;

    public RecordatorioController(RecordatorioService recordatorioService) {
        this.recordatorioService = recordatorioService;
    }

    @GetMapping
    public RecordatoriosResponse listar() {
        return recordatorioService.calcular();
    }
}
