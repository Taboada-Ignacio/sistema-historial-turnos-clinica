package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Paciente;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class PacienteSpecifications {

    private PacienteSpecifications() {
    }

    public static Specification<Paciente> busquedaAdmin(String texto, Long idProvincia, Long idLocalidad) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<?, ?> direccion = root.join("direccion", JoinType.INNER);
            Join<?, ?> localidad = direccion.join("localidad", JoinType.INNER);
            Join<?, ?> provincia = localidad.join("provincia", JoinType.INNER);

            if (idProvincia != null) {
                predicates.add(cb.equal(provincia.get("idProvincia"), idProvincia));
            }
            if (idLocalidad != null) {
                predicates.add(cb.equal(localidad.get("idLocalidad"), idLocalidad));
            }
            if (texto != null && !texto.isBlank()) {
                predicates.add(predicadoTextoApellidoNombre(root, cb, texto.trim().toLowerCase()));
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * Búsqueda para el portal profesional: obligatoria provincia y localidad del profesional;
     * {@code texto} por apellido/nombre o DNI (solo dígitos).
     */
    public static Specification<Paciente> busquedaEnUbicacion(String texto, Long idProvincia, Long idLocalidad) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<?, ?> direccion = root.join("direccion", JoinType.INNER);
            Join<?, ?> localidad = direccion.join("localidad", JoinType.INNER);
            Join<?, ?> provincia = localidad.join("provincia", JoinType.INNER);

            predicates.add(cb.equal(provincia.get("idProvincia"), idProvincia));
            predicates.add(cb.equal(localidad.get("idLocalidad"), idLocalidad));

            Join<?, ?> usuario = root.join("usuario", JoinType.INNER);
            Join<?, ?> estado = usuario.join("estadoActual", JoinType.INNER);
            predicates.add(cb.notEqual(cb.upper(estado.get("nombre")), "BLOQUEADO"));

            String normalizado = texto.trim();
            if (normalizado.matches("\\d+")) {
                String digitos = normalizado;
                List<Predicate> porDni = new ArrayList<>();
                try {
                    porDni.add(cb.equal(root.get("dni"), Integer.parseInt(digitos)));
                } catch (NumberFormatException ignored) {
                    // DNI fuera de rango int: solo coincidencia parcial como texto
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
            Root<Paciente> root, CriteriaBuilder cb, String normalizado) {
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
