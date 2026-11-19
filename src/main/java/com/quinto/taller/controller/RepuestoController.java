package com.quinto.taller.controller;

import com.quinto.taller.dto.RepuestoRequest;
import com.quinto.taller.model.Repuesto;
import com.quinto.taller.service.RepuestoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/** Inventario. Lectura: admin y mecánico. Escritura: solo admin (SecurityConfig). */
@RestController
@RequestMapping("/api/repuestos")
public class RepuestoController {

    private final RepuestoService repuestoService;

    public RepuestoController(RepuestoService repuestoService) {
        this.repuestoService = repuestoService;
    }

    @GetMapping
    public List<Repuesto> listar() {
        return repuestoService.listar();
    }

    @GetMapping("/{id}")
    public Repuesto buscar(@PathVariable Integer id) {
        return repuestoService.buscar(id);
    }

    @PostMapping
    public ResponseEntity<Repuesto> crear(@Valid @RequestBody RepuestoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(repuestoService.crear(req));
    }

    @PutMapping("/{id}")
    public Repuesto actualizar(@PathVariable Integer id, @Valid @RequestBody RepuestoRequest req) {
        return repuestoService.actualizar(id, req);
    }

    /** Entrada de mercadería (cantidad > 0) o ajuste por pérdida/conteo (cantidad < 0). */
    @PatchMapping("/{id}/stock")
    public Repuesto ajustarStock(@PathVariable Integer id, @RequestParam BigDecimal cantidad) {
        return repuestoService.ajustarStock(id, cantidad);
    }

    @DeleteMapping("/{id}")
    public Map<String, String> eliminar(@PathVariable Integer id) {
        repuestoService.eliminar(id);
        return Map.of("mensaje", "Repuesto eliminado");
    }
}
