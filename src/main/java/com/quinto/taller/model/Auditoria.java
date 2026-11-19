package com.quinto.taller.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Registro de auditoría: quién hizo qué, sobre qué y cuándo. */
@Entity
@Table(name = "auditoria", indexes = @Index(name = "idx_auditoria_fecha", columnList = "fecha"))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_auditoria")
    private Long idAuditoria;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 30, nullable = false)
    private String usuario;

    @Column(length = 20)
    private String rol;

    /** CREAR, ACTUALIZAR, ELIMINAR, ESTADO, STOCK, APROBAR, LOGIN, LOGIN_FALLIDO... */
    @Column(length = 20, nullable = false)
    private String accion;

    /** Cliente, Vehiculo, Orden, Repuesto, Usuario, Solicitud, Sesion */
    @Column(length = 20, nullable = false)
    private String entidad;

    @Column(name = "entidad_id", length = 30)
    private String entidadId;

    @Column(length = 300)
    private String descripcion;
}
