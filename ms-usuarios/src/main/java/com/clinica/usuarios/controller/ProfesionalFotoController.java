package com.clinica.usuarios.controller;

import com.clinica.usuarios.service.ProfesionalFotoStorageService;
import com.clinica.usuarios.service.ProfesionalService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

/**
 * Sirve fotos de perfil solo a profesionales y administradores autenticados (JWT).
 * Reemplaza el acceso público por carpeta estática.
 */
@RestController
@RequestMapping("/api/profesionales/fotos")
@RequiredArgsConstructor
public class ProfesionalFotoController {

    private final ProfesionalFotoStorageService fotoStorage;
    private final ProfesionalService profesionalService;

    /**
     * Foto de un profesional visible en el catálogo público (sin JWT).
     */
    @GetMapping("/public/{fileName}")
    public ResponseEntity<Resource> obtenerFotoPublica(@PathVariable String fileName) {
        if (!profesionalService.esFotoVisibleEnCatalogoPublico(fileName)) {
            return ResponseEntity.notFound().build();
        }
        Resource resource = fotoStorage.cargarPorNombreArchivo(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic())
                .body(resource);
    }

    @PreAuthorize("hasAnyAuthority('ROLE_PROFESIONAL', 'ROLE_ADMINISTRADOR')")
    @GetMapping("/{fileName}")
    public ResponseEntity<Resource> obtenerFoto(@PathVariable String fileName) {
        Resource resource = fotoStorage.cargarPorNombreArchivo(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("image/webp"))
                .cacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePrivate().mustRevalidate())
                .body(resource);
    }
}
