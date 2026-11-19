package com.quinto.taller.controller;

import com.quinto.taller.dto.UsuarioRequest;
import com.quinto.taller.model.Usuario;
import com.quinto.taller.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /** Lista de mecánicos para asignar órdenes (admin y mecánico). */
    @GetMapping("/mecanicos")
    public List<Usuario> mecanicos() {
        return usuarioService.listarMecanicos();
    }

    /* ---- Gestión de usuarios: solo ADMIN (restringido en SecurityConfig) ---- */

    @GetMapping("/usuarios")
    public List<Usuario> listar() {
        return usuarioService.listar();
    }

    @PostMapping("/usuarios")
    public ResponseEntity<Usuario> crear(@Valid @RequestBody UsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(req));
    }

    @DeleteMapping("/usuarios/{id}")
    public Map<String, String> eliminar(@PathVariable Integer id, Authentication auth) {
        usuarioService.eliminar(id, auth.getName());
        return Map.of("mensaje", "Usuario eliminado");
    }
}
