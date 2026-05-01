package com.clinica.usuarios.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "administradores")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
public class Administrador extends Usuario {
    
    // Por el momento no tiene atributos adicionales, hereda todo de Usuario.
    // La anotación @EqualsAndHashCode(callSuper = true) es necesaria 
    // para que Lombok compare correctamente usando los campos del padre.
}