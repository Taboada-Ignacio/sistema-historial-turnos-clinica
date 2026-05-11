package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Catálogo de direcciones tipificadas por localidad (similar a {@link Localidad} bajo {@link Provincia}).
 */
@Entity
@Table(name = "direcciones", uniqueConstraints = {
        @UniqueConstraint(name = "uk_direccion_nombre_localidad", columnNames = { "nombre", "id_localidad" })
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Direccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idDireccion;

    /** Texto mostrado en listas (calle, altura, barrio, etc.). */
    @Column(nullable = false, length = 500)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;
}
