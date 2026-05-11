package com.clinica.usuarios.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Expone en HTTP la carpeta local donde se guardan las fotos (mismo path relativo que {@code ProfesionalServiceImpl}).
 */
@Configuration
public class WebMvcStaticResourcesConfig implements WebMvcConfigurer {

    private static final String FOTOS_DIR = "fotosPerfilProfesionales";

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path dir = Paths.get(FOTOS_DIR).toAbsolutePath().normalize();
        String location = "file:" + dir.toString().replace("\\", "/") + "/";
        registry.addResourceHandler("/" + FOTOS_DIR + "/**").addResourceLocations(location);
    }
}
