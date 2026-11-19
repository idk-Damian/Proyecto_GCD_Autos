package com.quinto.taller.service;

import com.quinto.taller.dto.RegistroRequest;
import com.quinto.taller.dto.UsuarioRequest;
import com.quinto.taller.util.ValidadorEcuador;
import com.quinto.taller.exception.ConflictoException;
import com.quinto.taller.exception.RecursoNoEncontradoException;
import com.quinto.taller.model.Catalogos;
import com.quinto.taller.model.Usuario;
import com.quinto.taller.model.Cliente;
import com.quinto.taller.repository.ClienteRepository;
import com.quinto.taller.repository.MantenimientoRepository;
import com.quinto.taller.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoria;

    public UsuarioService(UsuarioRepository usuarioRepository, MantenimientoRepository mantenimientoRepository,
                          PasswordEncoder passwordEncoder, ClienteRepository clienteRepository,
                          AuditoriaService auditoria) {
        this.usuarioRepository = usuarioRepository;
        this.mantenimientoRepository = mantenimientoRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.auditoria = auditoria;
    }

    /** Resultado de crear un acceso: la contraseña temporal solo se muestra una vez. */
    public record AccesoCreado(String username, String passwordTemporal) {}

    /** El administrador crea la cuenta del portal para un cliente (usuario = cédula). */
    @Transactional
    public AccesoCreado crearAccesoCliente(String cedula) {
        Cliente c = clienteRepository.findById(cedula)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el cliente " + cedula));
        if (usuarioRepository.existsByClienteCedula(cedula) || usuarioRepository.existsByUsername(cedula)) {
            throw new ConflictoException("El cliente ya tiene acceso al portal");
        }
        String temporal = generarPassword();
        Usuario u = new Usuario();
        u.setUsername(cedula);
        u.setPassword(passwordEncoder.encode(temporal));
        u.setRol(Catalogos.ROL_CLIENTE);
        u.setNombreCompleto(c.getNombre() + " " + c.getApellido());
        u.setCliente(c);
        usuarioRepository.save(u);
        auditoria.registrar("CREAR", "Usuario", cedula, "Acceso al portal para " + u.getNombreCompleto());
        return new AccesoCreado(cedula, temporal);
    }

    /** Resultado del registro: indica si se creó una ficha nueva o se vinculó una existente. */
    public record RegistroResultado(Usuario usuario, boolean clienteExistente) {}

    /**
     * Registro público desde /registro.
     * - Cédula nueva: se crea la ficha del cliente y su cuenta en un solo paso.
     * - Cédula que el taller ya tenía: se exige que el teléfono o el correo coincidan con los registrados,
     *   para que nadie pueda ver el historial de otra persona conociendo solo su cédula.
     */
    @Transactional
    public RegistroResultado registrarCliente(RegistroRequest req) {
        String ced = req.cedula() == null ? "" : req.cedula().trim();
        if (!ced.matches("\\d{10}")) {
            throw new IllegalArgumentException("La cédula debe tener 10 dígitos.");
        }
        // El dígito verificador se exige a los clientes nuevos; las fichas que el taller ya tenía se aceptan
        // tal cual (se verifican por teléfono/correo más abajo).
        if (!clienteRepository.existsById(ced) && !ValidadorEcuador.cedulaValida(ced)) {
            throw new IllegalArgumentException("La cédula ingresada no es válida.");
        }
        if (usuarioRepository.existsByClienteCedula(ced) || usuarioRepository.existsByUsername(ced)) {
            throw new ConflictoException("Ya existe una cuenta con esta cédula. Inicie sesión con su cédula y contraseña.");
        }
        validarPassword(req.password());
        if (!req.password().equals(req.confirmacion())) {
            throw new IllegalArgumentException("Las contraseñas no coinciden.");
        }
        String telefono = limpiar(req.telefono());
        String correo = limpiar(req.correo());
        if (telefono == null || !telefono.matches("\\d{7,10}")) {
            throw new IllegalArgumentException("Ingrese un teléfono válido (7 a 10 dígitos).");
        }
        if (correo != null && !correo.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("El correo no tiene un formato válido.");
        }

        Cliente c = clienteRepository.findById(ced).orElse(null);
        boolean existente = c != null;
        if (existente) {
            boolean coincideTel = c.getTelefono() != null && c.getTelefono().trim().equals(telefono);
            boolean coincideCorreo = correo != null && c.getCorreo() != null && c.getCorreo().trim().equalsIgnoreCase(correo);
            if (!coincideTel && !coincideCorreo) {
                throw new IllegalArgumentException("Esta cédula ya está registrada en el taller, pero el teléfono y el correo "
                        + "no coinciden con los que dejó en recepción. Use esos datos o pida al taller que active su acceso.");
            }
        } else {
            String nombre = limpiar(req.nombre()), apellido = limpiar(req.apellido());
            if (nombre == null || apellido == null) {
                throw new IllegalArgumentException("Ingrese sus nombres y apellidos.");
            }
            if (nombre.length() > 50 || apellido.length() > 50 || (correo != null && correo.length() > 50)) {
                throw new IllegalArgumentException("Nombre, apellido y correo admiten máximo 50 caracteres.");
            }
            c = new Cliente();
            c.setCedula(ced);
            c.setNombre(nombre);
            c.setApellido(apellido);
            c.setTelefono(telefono);
            c.setCorreo(correo);
            c = clienteRepository.save(c);
            auditoria.registrarComo(ced, "CLIENTE", "CREAR", "Cliente", ced, "Autorregistro: " + nombre + " " + apellido);
        }

        Usuario u = new Usuario();
        u.setUsername(ced);
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRol(Catalogos.ROL_CLIENTE);
        u.setNombreCompleto(c.getNombre() + " " + c.getApellido());
        u.setCliente(c);
        usuarioRepository.save(u);
        auditoria.registrarComo(ced, "CLIENTE", "CREAR", "Usuario", ced,
                existente ? "Registro en el portal (cliente ya existente, verificado por contacto)" : "Registro en el portal (cliente nuevo)");
        return new RegistroResultado(u, existente);
    }

    private String limpiar(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    @Transactional
    public void cambiarPassword(String username, String actual, String nueva) {
        Usuario u = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado"));
        if (actual == null || !passwordEncoder.matches(actual, u.getPassword())) {
            throw new IllegalArgumentException("La contraseña actual no es correcta");
        }
        validarPassword(nueva);
        u.setPassword(passwordEncoder.encode(nueva));
        usuarioRepository.save(u);
        auditoria.registrar("ACTUALIZAR", "Usuario", username, "Cambio de contraseña");
    }

    @Transactional
    public void eliminarAccesoCliente(String cedula) {
        List<Usuario> cuentas = usuarioRepository.findByClienteCedula(cedula);
        if (cuentas.isEmpty()) throw new RecursoNoEncontradoException("El cliente no tiene acceso al portal");
        usuarioRepository.deleteAll(cuentas);
        auditoria.registrar("ELIMINAR", "Usuario", cedula, "Se quitó el acceso al portal");
    }

    private void validarPassword(String p) {
        if (p == null || p.length() < 6 || p.length() > 60) {
            throw new IllegalArgumentException("La contraseña debe tener entre 6 y 60 caracteres");
        }
    }

    private String generarPassword() {
        String chars = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ23456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) sb.append(chars.charAt(r.nextInt(chars.length())));
        return sb.toString();
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public List<Usuario> listarMecanicos() {
        return usuarioRepository.findByRolOrderByUsernameAsc(Catalogos.ROL_MECANICO);
    }

    @Transactional
    public Usuario crear(UsuarioRequest req) {
        String username = req.username().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(username)) {
            throw new ConflictoException("El usuario '" + username + "' ya existe");
        }
        String rol = req.rol().startsWith("ROLE_") ? req.rol() : "ROLE_" + req.rol();
        if (!Catalogos.ROLES.contains(rol)) {
            throw new IllegalArgumentException("Rol no válido: " + req.rol());
        }
        Usuario u = new Usuario();
        u.setUsername(username);
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setRol(rol);
        u.setNombreCompleto(req.nombreCompleto().trim());
        Usuario guardado = usuarioRepository.save(u);
        auditoria.registrar("CREAR", "Usuario", username, u.getNombreCompleto() + " · " + rol.replace("ROLE_", ""));
        return guardado;
    }

    @Transactional
    public void eliminar(Integer idUsuario, String usernameActual) {
        Usuario u = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario"));
        if (u.getUsername().equals(usernameActual)) {
            throw new ConflictoException("No puede eliminar su propio usuario");
        }
        if (mantenimientoRepository.existsByMecanicoIdUsuario(idUsuario)) {
            throw new ConflictoException("El usuario tiene órdenes asignadas; reasígnelas antes de eliminarlo");
        }
        usuarioRepository.delete(u);
        auditoria.registrar("ELIMINAR", "Usuario", u.getUsername(), u.getNombreCompleto());
    }
}
