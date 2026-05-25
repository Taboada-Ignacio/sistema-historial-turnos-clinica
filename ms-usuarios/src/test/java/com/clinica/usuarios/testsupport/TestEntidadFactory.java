package com.clinica.usuarios.testsupport;

import com.clinica.usuarios.model.*;
import com.clinica.usuarios.service.support.UsuarioAltaHelper;

import java.time.LocalDate;
import java.util.Set;

public final class TestEntidadFactory {

    private TestEntidadFactory() {
    }

    public static Usuario nuevoUsuario(String email, String passwordHash, Estado estado, Set<Rol> roles) {
        return UsuarioAltaHelper.nuevoUsuario(email, passwordHash, estado, roles);
    }

    public static Paciente paciente(
            Usuario usuario,
            String nombre,
            String apellido,
            int dni,
            String telefono,
            LocalDate fechaNacimiento,
            Sexo sexo,
            Direccion direccion,
            ObraSocial obraSocial) {
        Paciente paciente = new Paciente();
        paciente.setUsuario(usuario);
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setDni(dni);
        paciente.setTelefono(telefono);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setSexo(sexo != null ? sexo.name() : null);
        paciente.setDireccion(direccion);
        paciente.setObraSocial(obraSocial);
        return paciente;
    }

    public static Profesional profesional(
            Usuario usuario,
            String nombre,
            String apellido,
            int dni,
            String telefono,
            LocalDate fechaNacimiento,
            Sexo sexo,
            Direccion direccion,
            String matricula,
            Especialidad especialidad,
            Membresia membresiaActual) {
        Profesional profesional = new Profesional();
        profesional.setUsuario(usuario);
        profesional.setNombre(nombre);
        profesional.setApellido(apellido);
        profesional.setDni(dni);
        profesional.setTelefono(telefono);
        profesional.setFechaNacimiento(fechaNacimiento);
        profesional.setSexo(sexo != null ? sexo.name() : null);
        profesional.setDireccion(direccion);
        profesional.setMatricula(matricula);
        profesional.setEspecialidad(especialidad);
        profesional.setMembresiaActual(membresiaActual);
        return profesional;
    }

    public static Administrador administrador(
            Usuario usuario,
            String nombre,
            String apellido,
            int dni,
            String telefono,
            LocalDate fechaNacimiento,
            Sexo sexo,
            Direccion direccion) {
        Administrador admin = new Administrador();
        admin.setUsuario(usuario);
        admin.setNombre(nombre);
        admin.setApellido(apellido);
        admin.setDni(dni);
        admin.setTelefono(telefono);
        admin.setFechaNacimiento(fechaNacimiento);
        admin.setSexo(sexo != null ? sexo.name() : null);
        admin.setDireccion(direccion);
        return admin;
    }
}
