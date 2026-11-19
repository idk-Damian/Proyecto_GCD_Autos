package com.quinto.taller.controller;

import com.quinto.taller.dto.ClienteRequest;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.service.ClienteService;
import com.quinto.taller.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final UsuarioService usuarioService;

    public ClienteController(ClienteService clienteService, UsuarioService usuarioService) {
        this.clienteService = clienteService;
        this.usuarioService = usuarioService;
    }

    /** Crea la cuenta del portal del cliente (solo ADMIN). Devuelve una contraseña temporal. */
    @PostMapping("/{cedula}/acceso")
    public ResponseEntity<UsuarioService.AccesoCreado> crearAcceso(@PathVariable String cedula) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearAccesoCliente(cedula));
    }

    @DeleteMapping("/{cedula}/acceso")
    public Map<String, String> quitarAcceso(@PathVariable String cedula) {
        usuarioService.eliminarAccesoCliente(cedula);
        return Map.of("mensaje", "Acceso eliminado");
    }

    @GetMapping
    public List<Cliente> listar() {
        return clienteService.listar();
    }

    @GetMapping("/{cedula}")
    public Cliente buscar(@PathVariable String cedula) {
        return clienteService.buscar(cedula);
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clienteService.crear(req));
    }

    @PutMapping("/{cedula}")
    public Cliente actualizar(@PathVariable String cedula, @Valid @RequestBody ClienteRequest req) {
        return clienteService.actualizar(cedula, req);
    }

    @DeleteMapping("/{cedula}")
    public Map<String, String> eliminar(@PathVariable String cedula) {
        clienteService.eliminar(cedula);
        return Map.of("mensaje", "Cliente eliminado");
    }
}
