package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "pacientes")
// Vincula el ID de esta tabla hija con el ID de la tabla padre (usuarios)
@PrimaryKeyJoinColumn(name = "id_paciente") 
@Data
@EqualsAndHashCode(callSuper = true) // Incluye los campos de Usuario en las comparaciones
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Paciente extends Usuario {

    // Relación: Muchos pacientes pueden pertenecer a una misma Obra Social
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_obra_social", nullable = false)
    private ObraSocial obraSocial;

    // Dato extra (Opcional): En los sistemas reales se suele pedir el número de afiliado
    @Column(name = "numero_afiliado")
    private String numeroAfiliado;
}