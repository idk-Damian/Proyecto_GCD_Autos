package com.quinto.taller.service;

import com.quinto.taller.model.Auditoria;
import com.quinto.taller.repository.AuditoriaRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Locale;

/**
 * Registro de auditoría. Los servicios llaman a registrar(...) en cada operación que modifica datos.
 * Se guarda en la misma transacción: si la operación falla, tampoco queda el registro.
 */
@Service
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;

    public AuditoriaService(AuditoriaRepository auditoriaRepository) {
        this.auditoriaRepository = auditoriaRepository;
    }

    public void registrar(String accion, String entidad, Object entidadId, String descripcion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth != null && auth.isAuthenticated() ? auth.getName() : "sistema";
        String rol = auth == null ? null : auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).findFirst().map(r -> r.replace("ROLE_", "")).orElse(null);
        registrarComo(usuario, rol, accion, entidad, entidadId, descripcion);
    }

    /** Para eventos sin sesión activa (inicio de sesión, registro público). */
    public void registrarComo(String usuario, String rol, String accion, String entidad, Object entidadId, String descripcion) {
        Auditoria a = new Auditoria();
        a.setFecha(LocalDateTime.now());
        a.setUsuario(recortar(usuario == null || usuario.isBlank() ? "anónimo" : usuario, 30));
        a.setRol(rol);
        a.setAccion(accion);
        a.setEntidad(entidad);
        a.setEntidadId(entidadId == null ? null : recortar(String.valueOf(entidadId), 30));
        a.setDescripcion(descripcion == null ? null : recortar(descripcion, 300));
        auditoriaRepository.save(a);
    }

    public List<Auditoria> buscar(LocalDate desde, LocalDate hasta, String usuario, String accion, String entidad, String texto) {
        LocalDateTime d = (desde != null ? desde : LocalDate.now().minusDays(30)).atStartOfDay();
        LocalDateTime h = (hasta != null ? hasta : LocalDate.now()).atTime(LocalTime.MAX);
        String t = texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
        return auditoriaRepository.findTop1000ByFechaBetweenOrderByFechaDesc(d, h).stream()
                .filter(a -> vacio(usuario) || a.getUsuario().equalsIgnoreCase(usuario.trim()))
                .filter(a -> vacio(accion) || a.getAccion().equals(accion))
                .filter(a -> vacio(entidad) || a.getEntidad().equals(entidad))
                .filter(a -> t.isEmpty() || (a.getDescripcion() != null && a.getDescripcion().toLowerCase(Locale.ROOT).contains(t))
                        || (a.getEntidadId() != null && a.getEntidadId().toLowerCase(Locale.ROOT).contains(t)))
                .toList();
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }

    private String recortar(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
