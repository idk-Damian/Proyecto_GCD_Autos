package com.quinto.taller.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Repuesto del inventario del taller.
 * El stock se descuenta automáticamente cuando el repuesto se usa en una orden de trabajo.
 */
@Entity
@Table(name = "repuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_repuesto")
    private Integer idRepuesto;

    @Column(length = 20, unique = true, nullable = false)
    private String codigo;

    @Column(length = 100, nullable = false)
    private String nombre;

    /** unidad, litro, galón, juego, kit... */
    @Column(length = 15, nullable = false)
    private String unidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal stock;

    @Column(name = "stock_minimo", nullable = false, precision = 10, scale = 2)
    private BigDecimal stockMinimo;

    @Column(name = "precio_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioVenta;

    public boolean isStockBajo() {
        return stock != null && stockMinimo != null && stock.compareTo(stockMinimo) <= 0;
    }
}
