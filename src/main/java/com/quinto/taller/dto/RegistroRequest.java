package com.quinto.taller.dto;

/** Datos del formulario público de registro de clientes (/registro). */
public record RegistroRequest(
        String cedula,
        String nombre,
        String apellido,
        String telefono,
        String correo,
        String password,
        String confirmacion
) {}
