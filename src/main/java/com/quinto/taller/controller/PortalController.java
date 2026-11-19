package com.quinto.taller.controller;

import com.quinto.taller.dto.*;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.model.Mantenimiento;
import com.quinto.taller.model.SolicitudServicio;
import com.quinto.taller.service.PortalService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** API del portal del cliente (solo ROLE_CLIENTE, restringido en SecurityConfig). */
@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/resumen")
    public PortalResumen resumen(Authentication auth) {
        return portalService.resumen(auth.getName());
    }

    @PostMapping("/ordenes/{id}/aprobar")
    public Mantenimiento aprobar(@PathVariable Integer id, Authentication auth) {
        return portalService.aprobarPresupuesto(auth.getName(), id);
    }

    @PostMapping("/solicitudes")
    public ResponseEntity<SolicitudServicio> solicitar(@Valid @RequestBody SolicitudRequest req, Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portalService.crearSolicitud(auth.getName(), req));
    }

    @PatchMapping("/solicitudes/{id}/cancelar")
    public SolicitudServicio cancelar(@PathVariable Integer id, Authentication auth) {
        return portalService.cancelarSolicitud(auth.getName(), id);
    }

    @PostMapping("/vehiculos")
    public ResponseEntity<com.quinto.taller.model.Auto> registrarVehiculo(@Valid @RequestBody VehiculoPortalRequest req,
                                                                         Authentication auth) {
        return ResponseEntity.status(HttpStatus.CREATED).body(portalService.registrarVehiculo(auth.getName(), req));
    }

    @PutMapping("/perfil")
    public Cliente perfil(@Valid @RequestBody PerfilRequest req, Authentication auth) {
        return portalService.actualizarPerfil(auth.getName(), req);
    }

    @PostMapping("/password")
    public Map<String, String> password(@Valid @RequestBody CambioPasswordRequest req, Authentication auth) {
        portalService.cambiarPassword(auth.getName(), req);
        return Map.of("mensaje", "Contraseña actualizada");
    }
}
