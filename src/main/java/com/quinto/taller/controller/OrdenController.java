package com.quinto.taller.controller;

import com.quinto.taller.dto.OrdenRequest;
import com.quinto.taller.model.Mantenimiento;
import com.quinto.taller.service.OrdenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Órdenes de trabajo. Se mantiene la ruta /api/mantenimientos por compatibilidad.
 */
@RestController
@RequestMapping("/api/mantenimientos")
public class OrdenController {

    private final OrdenService ordenService;

    public OrdenController(OrdenService ordenService) {
        this.ordenService = ordenService;
    }

    @GetMapping
    public List<Mantenimiento> listar() {
        return ordenService.listar();
    }

    @GetMapping("/{id}")
    public Mantenimiento buscar(@PathVariable Integer id) {
        return ordenService.buscar(id);
    }

    @GetMapping("/placa/{placa}")
    public List<Mantenimiento> buscarPorPlaca(@PathVariable String placa) {
        return ordenService.buscarPorPlaca(placa);
    }

    @GetMapping("/auto/{idAuto}")
    public List<Mantenimiento> buscarPorAuto(@PathVariable Integer idAuto) {
        return ordenService.buscarPorAuto(idAuto);
    }

    @PostMapping
    public ResponseEntity<Mantenimiento> crear(@Valid @RequestBody OrdenRequest req, Authentication auth) {
        Mantenimiento m = ordenService.crear(req, auth.getName(), Seguridad.esAdmin(auth));
        return ResponseEntity.status(HttpStatus.CREATED).body(m);
    }

    @PutMapping("/{id}")
    public Mantenimiento actualizar(@PathVariable Integer id, @Valid @RequestBody OrdenRequest req, Authentication auth) {
        return ordenService.actualizar(id, req, auth.getName(), Seguridad.esAdmin(auth));
    }

    @PatchMapping("/{id}/estado")
    public Mantenimiento cambiarEstado(@PathVariable Integer id, @RequestParam String estado, Authentication auth) {
        return ordenService.cambiarEstado(id, estado, auth.getName(), Seguridad.esAdmin(auth));
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Integer id) {
        ordenService.eliminar(id);
        return Map.of("mensaje", "Orden eliminada");
    }
}
