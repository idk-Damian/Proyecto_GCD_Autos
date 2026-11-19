package com.quinto.taller.controller;

import com.quinto.taller.dto.RegistroRequest;
import com.quinto.taller.repository.UsuarioRepository;
import com.quinto.taller.service.UsuarioService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class VistaController {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;

    /** Mostrar u ocultar el cuadro de credenciales de demostración en el login. */
    @Value("${app.demo.mostrar-credenciales:true}")
    private boolean mostrarCredencialesDemo;

    /** Datos del taller para el encabezado de comprobantes y reportes impresos. */
    @Value("${app.taller.nombre:Taller Mecánico}")
    private String tallerNombre;
    @Value("${app.taller.ruc:}")
    private String tallerRuc;
    @Value("${app.taller.direccion:}")
    private String tallerDireccion;
    @Value("${app.taller.telefono:}")
    private String tallerTelefono;

    public VistaController(UsuarioRepository usuarioRepository, UsuarioService usuarioService) {
        this.usuarioRepository = usuarioRepository;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("mostrarDemo", mostrarCredencialesDemo);
        return "login";
    }

    /* ---------------- Registro público de clientes ---------------- */

    @GetMapping("/registro")
    public String registro() {
        return "registro";
    }

    @PostMapping("/registro")
    public String registrar(@RequestParam String cedula,
                            @RequestParam(required = false) String nombre,
                            @RequestParam(required = false) String apellido,
                            @RequestParam(required = false) String telefono,
                            @RequestParam(required = false) String correo,
                            @RequestParam String password, @RequestParam String confirmacion,
                            RedirectAttributes ra) {
        try {
            UsuarioService.RegistroResultado r = usuarioService.registrarCliente(
                    new RegistroRequest(cedula, nombre, apellido, telefono, correo, password, confirmacion));
            return r.clienteExistente() ? "redirect:/login?registrado&vinculado" : "redirect:/login?registrado";
        } catch (RuntimeException e) {
            ra.addFlashAttribute("error", e.getMessage());
            ra.addFlashAttribute("cedula", cedula);
            ra.addFlashAttribute("nombre", nombre);
            ra.addFlashAttribute("apellido", apellido);
            ra.addFlashAttribute("telefono", telefono);
            ra.addFlashAttribute("correo", correo);
            return "redirect:/registro";
        }
    }

    /* ---------------- Vistas autenticadas ---------------- */

    @GetMapping("/")
    public String raiz(Authentication authentication) {
        return esCliente(authentication) ? "redirect:/portal" : "redirect:/taller";
    }

    @GetMapping("/taller")
    public String taller(Authentication authentication, Model model) {
        if (esCliente(authentication)) return "redirect:/portal";
        cargarComunes(authentication, model);
        model.addAttribute("esAdmin", Seguridad.esAdmin(authentication));
        return "index";
    }

    @GetMapping("/portal")
    public String portal(Authentication authentication, Model model) {
        cargarComunes(authentication, model);
        return "portal";
    }

    private void cargarComunes(Authentication authentication, Model model) {
        String username = authentication.getName();
        String nombre = usuarioRepository.findByUsername(username)
                .map(u -> u.getNombreCompleto() != null ? u.getNombreCompleto() : u.getUsername())
                .orElse(username);
        model.addAttribute("username", username);
        model.addAttribute("nombreUsuario", nombre);
        model.addAttribute("tallerNombre", tallerNombre);
        model.addAttribute("tallerRuc", tallerRuc);
        model.addAttribute("tallerDireccion", tallerDireccion);
        model.addAttribute("tallerTelefono", tallerTelefono);
    }

    private boolean esCliente(Authentication auth) {
        return auth != null && auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
    }
}
