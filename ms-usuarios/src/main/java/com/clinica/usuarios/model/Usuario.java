package com.clinica.usuarios.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;
import java.util.Set; // Asegúrate de importar Set

@Entity
@Table(name = "usuarios")
// Definimos la estrategia de herencia: JOINED creará una tabla por cada clase
// pero la tabla 'usuarios' contendrá todos los campos comunes.
@Inheritance(strategy = InheritanceType.JOINED)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder // Permite que las clases hijas hereden el builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idUsuario;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String apellido;

    @Column(nullable = false, unique = true)
    private Integer dni;
    
    @Column(nullable = false)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(nullable = false)
    private Boolean estado;

    // Relación con Rol: Un usuario puede tener uno o muchos roles
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "usuario_roles", // Nombre de la tabla intermedia en la base de datos
        joinColumns = @JoinColumn(name = "id_usuario"), // Clave foránea hacia la tabla 'usuarios'
        inverseJoinColumns = @JoinColumn(name = "id_rol") // Clave foránea hacia la tabla 'roles'
    )
    private Set<Rol> roles; // Cambiado de 'Rol' a 'Set<Rol>' y renombrado a plural

    // Relación con Localidad: Muchos usuarios viven en una localidad
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_localidad", nullable = false)
    private Localidad localidad;
}