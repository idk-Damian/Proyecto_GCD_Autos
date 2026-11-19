package com.quinto.taller.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Solicitud de servicio (cita) que el cliente envía desde el portal.
 * El taller la convierte en una orden de trabajo o la rechaza.
 */
@Entity
@Table(name = "solicitud_servicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_solicitud")
    private Integer idSolicitud;

    @ManyToOne(optional = false)
    @JoinColumn(name = "auto_id")
    private Auto auto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_cedula")
    private Cliente cliente;

    @Column(name = "fecha_solicitud", nullable = false)
    private LocalDateTime fechaSolicitud;

    @Column(name = "fecha_preferida", nullable = false)
    private LocalDate fechaPreferida;

    /** Mañana o Tarde */
    @Column(length = 10, nullable = false)
    private String franja;

    @Column(name = "tipo_mantenimiento", length = 30, nullable = false)
    private String tipoMantenimiento;

    @Column(length = 300, nullable = false)
    private String descripcion;

    /** PENDIENTE, ATENDIDA, RECHAZADA o CANCELADA */
    @Column(length = 15, nullable = false)
    private String estado;

    /** Respuesta del taller (motivo de rechazo, indicaciones). */
    @Column(length = 200)
    private String respuesta;

    /** Orden de trabajo generada a partir de la solicitud (sin FK para no bloquear borrados). */
    @Column(name = "id_orden")
    private Integer idOrden;
}
