package com.clinica.usuarios.repository;

import com.clinica.usuarios.model.CambioEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CambioEstadoRepository extends JpaRepository<CambioEstado, Long> {
    
    // Para mostrarle al administrador el historial de un usuario ordenado por fecha
    List<CambioEstado> findByUsuario_IdUsuarioOrderByFechaDesc(Long idUsuario);

    void deleteByUsuario_IdUsuario(Long idUsuario);
}