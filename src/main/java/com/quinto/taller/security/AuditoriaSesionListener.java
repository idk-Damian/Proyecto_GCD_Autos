package com.quinto.taller.security;

import com.quinto.taller.service.AuditoriaService;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/** Registra en la auditoría los inicios de sesión exitosos y fallidos. */
@Component
public class AuditoriaSesionListener {

    private final AuditoriaService auditoriaService;

    public AuditoriaSesionListener(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @EventListener
    public void loginExitoso(AuthenticationSuccessEvent e) {
        String rol = e.getAuthentication().getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).findFirst().map(r -> r.replace("ROLE_", "")).orElse(null);
        auditoriaService.registrarComo(e.getAuthentication().getName(), rol, "LOGIN", "Sesion", null, "Inicio de sesión");
    }

    @EventListener
    public void loginFallido(AbstractAuthenticationFailureEvent e) {
        auditoriaService.registrarComo(e.getAuthentication().getName(), null, "LOGIN_FALLIDO", "Sesion", null,
                "Intento fallido: " + e.getException().getMessage());
    }
}
