package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Administrador;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class AdministradorSpecifications {

    private AdministradorSpecifications() {
    }

    public static Specification<Administrador> busquedaEnUbicacion(
            String texto, Long idProvincia, Long idLocalidad) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            Join<?, ?> direccion = root.join("direccion", JoinType.INNER);
            Join<?, ?> localidad = direccion.join("localidad", JoinType.INNER);
            Join<?, ?> provincia = localidad.join("provincia", JoinType.INNER);

            predicates.add(cb.equal(provincia.get("idProvincia"), idProvincia));
            predicates.add(cb.equal(localidad.get("idLocalidad"), idLocalidad));
            predicates.add(predicadoTexto(texto, root, cb));

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static Predicate predicadoTexto(String texto, Root<Administrador> root, jakarta.persistence.criteria.CriteriaBuilder cb) {
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
            return cb.or(porDni.toArray(new Predicate[0]));
        }
        String patternCompleto = "%" + normalizado.toLowerCase() + "%";
        Predicate porApellidoONombre = cb.or(
                cb.like(cb.lower(root.get("apellido")), patternCompleto),
                cb.like(cb.lower(root.get("nombre")), patternCompleto));
        String[] partes = normalizado.toLowerCase().split("\\s+");
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
