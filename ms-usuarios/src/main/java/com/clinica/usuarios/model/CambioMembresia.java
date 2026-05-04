package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cambios_membresia")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CambioMembresia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCambioMembresia;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    // Puede ser null para membresías como "Sin verificar" o "Acceso indefinido"
    @Column(name = "fecha_vencimiento") 
    private LocalDateTime fechaVencimiento; 

    // Relación con el Profesional (Dueño del cambio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_profesional", nullable = false)
    private Profesional profesional;

    // Relación con el tipo de membresía asignada en este cambio
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_membresia", nullable = false)
    private Membresia membresia;
}