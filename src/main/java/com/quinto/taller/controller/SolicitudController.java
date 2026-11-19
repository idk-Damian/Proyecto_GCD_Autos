package com.quinto.taller.controller;

import com.quinto.taller.model.SolicitudServicio;
import com.quinto.taller.service.SolicitudService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Bandeja de solicitudes del portal para el personal del taller. */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    public List<SolicitudServicio> listar() {
        return solicitudService.listar();
    }

    @PatchMapping("/{id}/rechazar")
    public SolicitudServicio rechazar(@PathVariable Integer id, @RequestParam String motivo) {
        return solicitudService.rechazar(id, motivo);
    }
}
