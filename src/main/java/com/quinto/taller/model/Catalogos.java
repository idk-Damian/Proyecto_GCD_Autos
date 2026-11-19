package com.quinto.taller.model;

import java.util.List;

/**
 * Valores permitidos para los campos de estado/tipo.
 * Se guardan como texto en la BD (compatibles con los datos existentes),
 * pero el backend solo acepta estos valores.
 */
public final class Catalogos {

    private Catalogos() {}

    // Estados del vehículo dentro del taller
    public static final String AUTO_INGRESADO = "Ingresado";
    public static final String AUTO_DIAGNOSTICO = "En Diagnóstico";
    public static final String AUTO_REPARACION = "En Reparación";
    public static final String AUTO_LISTO = "Listo";
    public static final String AUTO_ENTREGADO = "Entregado";
    public static final List<String> ESTADOS_AUTO = List.of(
            AUTO_INGRESADO, AUTO_DIAGNOSTICO, AUTO_REPARACION, AUTO_LISTO, AUTO_ENTREGADO);

    // Estados de la orden de trabajo
    public static final String ORDEN_PENDIENTE = "Pendiente";
    public static final String ORDEN_EN_PROCESO = "En Proceso";
    public static final String ORDEN_FINALIZADO = "Finalizado";
    public static final List<String> ESTADOS_ORDEN = List.of(ORDEN_PENDIENTE, ORDEN_EN_PROCESO, ORDEN_FINALIZADO);

    // Tipos de mantenimiento
    public static final List<String> TIPOS_MANTENIMIENTO = List.of("Preventivo", "Correctivo");

    // Tipos de línea de detalle de una orden
    public static final String DETALLE_REPUESTO = "REPUESTO";
    public static final String DETALLE_MANO_OBRA = "MANO_OBRA";
    public static final List<String> TIPOS_DETALLE = List.of(DETALLE_REPUESTO, DETALLE_MANO_OBRA);

    // Roles
    public static final String ROL_ADMIN = "ROLE_ADMIN";
    public static final String ROL_MECANICO = "ROLE_MECANICO";
    public static final String ROL_CLIENTE = "ROLE_CLIENTE";
    /** Roles que el administrador puede asignar desde la pestaña Usuarios (personal del taller). */
    public static final List<String> ROLES = List.of(ROL_ADMIN, ROL_MECANICO);

    // Solicitudes de servicio hechas por el cliente desde el portal
    public static final String SOL_PENDIENTE = "PENDIENTE";
    public static final String SOL_ATENDIDA = "ATENDIDA";
    public static final String SOL_RECHAZADA = "RECHAZADA";
    public static final String SOL_CANCELADA = "CANCELADA";
}
