package com.clinica.usuarios.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Almacenamiento local de fotos de perfil (preparado para migrar a object storage en producción).
 */
public interface ProfesionalFotoStorageService {

    String guardar(MultipartFile foto);

    void borrarSiExiste(String rutaPublica);

    Resource cargarPorNombreArchivo(String fileName);
}
