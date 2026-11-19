package com.quinto.taller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * Línea de una orden de trabajo: un repuesto o un trabajo de mano de obra.
 */
@Entity
@Table(name = "detalle_orden")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleOrden {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Integer idDetalle;

    /** REPUESTO o MANO_OBRA */
    @Column(length = 15, nullable = false)
    private String tipo;

    @Column(length = 120, nullable = false)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal cantidad;

    @Column(name = "precio_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    /** Repuesto del inventario (opcional: null en mano de obra o repuestos no inventariados). */
    @ManyToOne
    @JoinColumn(name = "repuesto_id")
    @JsonIgnoreProperties({"stock", "stockMinimo", "stockBajo", "precioVenta", "unidad"})
    private Repuesto repuesto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "mantenimiento_id")
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Mantenimiento mantenimiento;

    public BigDecimal getSubtotal() {
        if (cantidad == null || precioUnitario == null) return BigDecimal.ZERO;
        return cantidad.multiply(precioUnitario);
    }
}
