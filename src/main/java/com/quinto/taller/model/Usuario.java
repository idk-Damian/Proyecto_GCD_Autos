package com.quinto.taller.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Integer idUsuario;

    @Column(length = 30, unique = true, nullable = false)
    private String username;

    /** Nunca se envía en las respuestas JSON. */
    @Column(nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private String password;

    @Column(length = 20)
    private String rol;

    @Column(name = "nombre_completo", length = 80)
    private String nombreCompleto;

    /** Solo para usuarios con rol CLIENTE: el cliente (propietario) al que pertenece la cuenta. */
    @ManyToOne
    @JoinColumn(name = "cliente_cedula")
    private Cliente cliente;
}
