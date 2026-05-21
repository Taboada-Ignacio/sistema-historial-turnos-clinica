package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.Paciente;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
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
                String normalizado = texto.trim().toLowerCase();
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
                    predicates.add(cb.or(porApellidoONombre, apellidoYNombre));
                } else {
                    predicates.add(porApellidoONombre);
                }
            }

            query.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
