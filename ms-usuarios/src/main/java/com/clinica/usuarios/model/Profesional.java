package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "profesionales")
// Le decimos a Hibernate que el ID de esta tabla será el mismo que el idUsuario heredado
@PrimaryKeyJoinColumn(name = "id_profesional") 
@Data
@EqualsAndHashCode(callSuper = true) // Importante en Lombok cuando usamos herencia con @Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Profesional extends Usuario {

    @Column(name = "nro_matricula", nullable = false, unique = true)
    private Integer nroMatricula;

    // Relación: Muchos profesionales pueden tener la misma especialidad
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_especialidad", nullable = false)
    private Especialidad especialidad;
}