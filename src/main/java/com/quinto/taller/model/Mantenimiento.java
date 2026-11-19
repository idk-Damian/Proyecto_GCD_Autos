package com.quinto.taller.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Orden de trabajo (mantenimiento) de un vehículo.
 */
@Entity
@Table(name = "mantenimiento")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mantenimiento")
    private Integer idMantenimiento;

    @Column(name = "fecha_ingreso")
    private LocalDate fechaIngreso;

    /** Fecha en que la orden pasó a Finalizado (se usa para reportes de ingresos). */
    @Column(name = "fecha_finalizacion")
    private LocalDate fechaFinalizacion;

    /** Kilometraje del vehículo al ingresar al taller (base para los recordatorios). */
    private Integer kilometraje;

    @Column(name = "tipo_mantenimiento", length = 30)
    private String tipoMantenimiento;

    @Column(length = 200)
    private String diagnostico;

    @Column(name = "trabajos_realizados", length = 200)
    private String trabajosRealizados;

    /** Total de la orden. Si la orden tiene líneas de detalle, se calcula como la suma de ellas. */
    @Column(precision = 10, scale = 2)
    private BigDecimal costo;

    @Column(length = 20)
    private String estado;

    /** El cliente aprobó el presupuesto desde el portal. */
    @Column(name = "aprobado_cliente")
    private Boolean aprobadoCliente;

    @Column(name = "fecha_aprobacion")
    private LocalDateTime fechaAprobacion;

    @ManyToOne
    @JoinColumn(name = "auto_id")
    private Auto auto;

    /** Mecánico responsable de la orden (opcional). */
    @ManyToOne
    @JoinColumn(name = "mecanico_id")
    private Usuario mecanico;

    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("idDetalle ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<DetalleOrden> detalles = new ArrayList<>();
}
