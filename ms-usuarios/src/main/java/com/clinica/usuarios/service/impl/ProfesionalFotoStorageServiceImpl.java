package com.clinica.usuarios.service.impl;

import com.clinica.usuarios.exception.RecursoNoEncontradoException;
import com.clinica.usuarios.exception.ReglaDeNegocioException;
import com.clinica.usuarios.service.ProfesionalFotoStorageService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ProfesionalFotoStorageServiceImpl implements ProfesionalFotoStorageService {

    private static final Pattern NOMBRE_ARCHIVO_SEGURO =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.webp$");

    private final String storageDirName;
    private final long maxSizeBytes;
    private Path storageRoot;

    public ProfesionalFotoStorageServiceImpl(
            @Value("${app.profesional-foto.storage-dir:fotosPerfilProfesionales}") String storageDirName,
            @Value("${app.profesional-foto.max-size-bytes:2097152}") long maxSizeBytes) {
        this.storageDirName = storageDirName;
        this.maxSizeBytes = maxSizeBytes;
    }

    @PostConstruct
    void initStorage() throws IOException {
        storageRoot = Paths.get(storageDirName).toAbsolutePath().normalize();
        Files.createDirectories(storageRoot);
        log.info("Fotos de perfil profesional: directorio {}", storageRoot);
    }

    @Override
    public String guardar(MultipartFile foto) {
        byte[] contenido = leerYValidar(foto);
        String nombreArchivo = UUID.randomUUID() + ".webp";
        Path destino = resolverArchivoSeguro(nombreArchivo);
        try {
            Files.write(destino, contenido);
        } catch (IOException e) {
            throw new ReglaDeNegocioException("No se pudo guardar la foto de perfil.");
        }
        return "/" + storageDirName + "/" + nombreArchivo;
    }

    @Override
    public void borrarSiExiste(String rutaPublica) {
        if (rutaPublica == null || rutaPublica.isBlank()) {
            return;
        }
        String fileName = extraerNombreArchivo(rutaPublica);
        if (fileName == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolverArchivoSeguro(fileName));
        } catch (IOException | ReglaDeNegocioException e) {
            log.debug("No se pudo borrar foto {}: {}", fileName, e.getMessage());
        }
    }

    @Override
    public Resource cargarPorNombreArchivo(String fileName) {
        validarNombreArchivo(fileName);
        Path archivo = resolverArchivoSeguro(fileName);
        if (!Files.isRegularFile(archivo)) {
            throw new RecursoNoEncontradoException("Foto de perfil no encontrada.");
        }
        return new FileSystemResource(archivo);
    }

    private byte[] leerYValidar(MultipartFile foto) {
        if (foto == null || foto.isEmpty()) {
            throw new ReglaDeNegocioException("La foto está vacía.");
        }
        if (foto.getSize() > maxSizeBytes) {
            throw new ReglaDeNegocioException(
                    "La foto supera el tamaño máximo permitido (" + (maxSizeBytes / 1024 / 1024) + " MB).");
        }
        String original = foto.getOriginalFilename();
        if (original != null && !original.toLowerCase().endsWith(".webp")) {
            throw new ReglaDeNegocioException("La foto de perfil debe ser en formato WebP (.webp).");
        }
        try {
            byte[] contenido = foto.getBytes();
            if (contenido.length > maxSizeBytes) {
                throw new ReglaDeNegocioException(
                        "La foto supera el tamaño máximo permitido (" + (maxSizeBytes / 1024 / 1024) + " MB).");
            }
            if (!esWebp(contenido)) {
                throw new ReglaDeNegocioException("El archivo no es una imagen WebP válida.");
            }
            return contenido;
        } catch (IOException e) {
            throw new ReglaDeNegocioException("No se pudo leer la foto enviada.");
        }
    }

    private static boolean esWebp(byte[] data) {
        if (data.length < 12) {
            return false;
        }
        return data[0] == 'R' && data[1] == 'I' && data[2] == 'F' && data[3] == 'F'
                && data[8] == 'W' && data[9] == 'E' && data[10] == 'B' && data[11] == 'P';
    }

    private Path resolverArchivoSeguro(String fileName) {
        validarNombreArchivo(fileName);
        Path resolved = storageRoot.resolve(fileName).normalize();
        if (!resolved.startsWith(storageRoot)) {
            throw new ReglaDeNegocioException("Nombre de archivo no válido.");
        }
        return resolved;
    }

    private static void validarNombreArchivo(String fileName) {
        if (fileName == null || !NOMBRE_ARCHIVO_SEGURO.matcher(fileName).matches()) {
            throw new ReglaDeNegocioException("Nombre de archivo no válido.");
        }
    }

    private String extraerNombreArchivo(String rutaPublica) {
        String rel = rutaPublica.startsWith("/") ? rutaPublica.substring(1) : rutaPublica;
        if (!rel.startsWith(storageDirName + "/")) {
            return null;
        }
        String fileName = rel.substring(storageDirName.length() + 1);
        if (!NOMBRE_ARCHIVO_SEGURO.matcher(fileName).matches()) {
            return null;
        }
        return fileName;
    }
}
