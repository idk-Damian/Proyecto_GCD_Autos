package com.quinto.taller.controller;

import com.quinto.taller.dto.AutoRequest;
import com.quinto.taller.model.Auto;
import com.quinto.taller.service.AutoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/autos")
public class AutoController {

    private final AutoService autoService;

    public AutoController(AutoService autoService) {
        this.autoService = autoService;
    }

    @GetMapping
    public List<Auto> listar() {
        return autoService.listar();
    }

    @GetMapping("/{idAuto}")
    public Auto buscar(@PathVariable Integer idAuto) {
        return autoService.buscar(idAuto);
    }

    @GetMapping("/placa/{placa}")
    public Auto buscarPorPlaca(@PathVariable String placa) {
        return autoService.buscarPorPlaca(placa);
    }

    @GetMapping("/cliente/{cedula}")
    public List<Auto> buscarPorCliente(@PathVariable String cedula) {
        return autoService.buscarPorCliente(cedula);
    }

    @GetMapping("/estado/{estado}")
    public List<Auto> buscarPorEstado(@PathVariable String estado) {
        return autoService.buscarPorEstado(estado);
    }

    @PostMapping
    public ResponseEntity<Auto> crear(@Valid @RequestBody AutoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(autoService.crear(req));
    }

    @PutMapping("/{idAuto}")
    public Auto actualizar(@PathVariable Integer idAuto, @Valid @RequestBody AutoRequest req, Authentication auth) {
        return autoService.actualizar(idAuto, req, Seguridad.esAdmin(auth));
    }

    @PatchMapping("/{idAuto}/estado")
    public Auto actualizarEstado(@PathVariable Integer idAuto,
                                 @RequestParam(required = false) String estado,
                                 @RequestParam(required = false) Integer kilometraje) {
        return autoService.actualizarEstado(idAuto, estado, kilometraje);
    }

    @DeleteMapping("/{idAuto}")
    public Map<String, String> eliminar(@PathVariable Integer idAuto) {
        autoService.eliminar(idAuto);
        return Map.of("mensaje", "Vehículo eliminado");
    }
}
