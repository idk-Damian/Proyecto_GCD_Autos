package com.quinto.taller.controller;

import org.springframework.security.core.Authentication;

/** Utilidad para consultar el rol del usuario autenticado. */
final class Seguridad {

    private Seguridad() {}

    static boolean esAdmin(Authentication auth) {
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}
