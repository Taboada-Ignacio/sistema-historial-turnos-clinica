package com.clinica.usuarios.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PacienteUpdateDTO {

    // En las actualizaciones, a veces los campos pueden venir nulos si el usuario no los quiso cambiar.
    // Usamos validaciones que aplican SOLO si el campo tiene contenido.

    @Email(message = "El formato del email no es válido")
    private String email;

    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$", 
             message = "La contraseña debe contener al menos un dígito, una minúscula, una mayúscula y un carácter especial")
    private String password;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "El formato del teléfono no es válido")
    private String telefono;

    private Boolean estado;
}