package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Entity
@Table(name = "profesionales")
@PrimaryKeyJoinColumn(name = "id_profesional") 
@Data
@EqualsAndHashCode(callSuper = true) 
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Profesional extends Usuario {

    @Column(name = "nro_matricula", nullable = false, unique = true)
    private String matricula;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;

    // --- NUEVO CAMPO PARA LA FOTO ---
    @Column(name = "foto_perfil")
    private String fotoPerfil; // Guardará la URL o ruta (ej: "/uploads/profesionales/123.webp")

    // --- NUEVAS RELACIONES DE MEMBRESÍA ---

    // Atajo para saber la membresía actual sin tener que buscar el último registro del historial
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_membresia_actual", nullable = false)
    private Membresia membresiaActual;

    // Historial de todos los pagos/cambios (El 1 a N de tu diagrama)
    @OneToMany(mappedBy = "profesional", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CambioMembresia> historialMembresias;
}