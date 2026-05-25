package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Administrador;
import com.clinica.usuarios.model.Paciente;
import com.clinica.usuarios.model.Profesional;
import com.clinica.usuarios.model.Usuario;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class UsuarioSpecifications {

    private UsuarioSpecifications() {
    }

    private static final String ESTADO_BLOQUEADO = "BLOQUEADO";

    /**
     * Pacientes y profesionales en la zona (cualquier estado salvo BLOQUEADO), administradores sin filtro
     * de estado; búsqueda por apellido, nombre o DNI.
     */
    public static Specification<Usuario> busquedaPersonasEnUbicacion(
            String texto, Long idProvincia, Long idLocalidad) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<?, ?> direccion = root.join("direccion", JoinType.INNER);
            Join<?, ?> localidad = direccion.join("localidad", JoinType.INNER);
            Join<?, ?> provincia = localidad.join("provincia", JoinType.INNER);
            Join<?, ?> estado = root.join("estadoActual", JoinType.INNER);

            predicates.add(cb.equal(provincia.get("idProvincia"), idProvincia));
            predicates.add(cb.equal(localidad.get("idLocalidad"), idLocalidad));

            Predicate noBloqueado = cb.notEqual(cb.upper(estado.get("nombre")), ESTADO_BLOQUEADO);
            Predicate pacienteEnZona = cb.and(cb.equal(root.type(), Paciente.class), noBloqueado);
            Predicate profesionalEnZona = cb.and(cb.equal(root.type(), Profesional.class), noBloqueado);
            Predicate administradorEnZona = cb.equal(root.type(), Administrador.class);
            predicates.add(cb.or(pacienteEnZona, profesionalEnZona, administradorEnZona));

            String normalizado = texto.trim();
            if (normalizado.matches("\\d+")) {
                String digitos = normalizado;
                List<Predicate> porDni = new ArrayList<>();
                try {
                    porDni.add(cb.equal(root.get("dni"), Integer.parseInt(digitos)));
                } catch (NumberFormatException ignored) {
                    // solo coincidencia parcial
                }
                porDni.add(cb.like(root.get("dni").as(String.class), "%" + digitos + "%"));
                predicates.add(cb.or(porDni.toArray(new Predicate[0])));
            } else {
                predicates.add(predicadoTextoApellidoNombre(root, cb, normalizado.toLowerCase()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate predicadoTextoApellidoNombre(
            Root<Usuario> root, CriteriaBuilder cb, String normalizado) {
        String patternCompleto = "%" + normalizado + "%";
        Predicate porApellidoONombre = cb.or(
                cb.like(cb.lower(root.get("apellido")), patternCompleto),
                cb.like(cb.lower(root.get("nombre")), patternCompleto));

        String[] partes = normalizado.split("\\s+");
        if (partes.length >= 2) {
            String patApellido = "%" + partes[0] + "%";
            String patNombre = "%" + partes[partes.length - 1] + "%";
            Predicate apellidoYNombre = cb.and(
                    cb.like(cb.lower(root.get("apellido")), patApellido),
                    cb.like(cb.lower(root.get("nombre")), patNombre));
            return cb.or(porApellidoONombre, apellidoYNombre);
        }
        return porApellidoONombre;
    }
}
