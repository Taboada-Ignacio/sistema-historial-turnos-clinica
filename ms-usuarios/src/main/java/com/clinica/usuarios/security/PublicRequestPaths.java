package com.clinica.usuarios.security;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Normaliza la ruta de la petición y detecta endpoints públicos (sin JWT).
 * Cubre servletPath, requestURI y prefijo {@code /usuarios} si el gateway no hace StripPrefix.
 */
public final class PublicRequestPaths {

    private PublicRequestPaths() {
    }

    public static String normalizedPath(HttpServletRequest request) {
        String servlet = request.getServletPath();
        String uri = request.getRequestURI();
        String path = (servlet != null && !servlet.isBlank()) ? servlet : uri;
        if (path == null) {
            return "";
        }
        String ctx = request.getContextPath();
        if (ctx != null && !ctx.isBlank() && path.startsWith(ctx)) {
            path = path.substring(ctx.length());
        }
        if (path.startsWith("/usuarios/")) {
            path = path.substring("/usuarios".length());
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    public static boolean isAuthPublicPath(String path) {
        return path != null && (path.startsWith("/api/auth/") || "/api/auth".equals(path));
    }

    public static boolean isAdminOnboardingPublicPath(String path) {
        return path != null && path.startsWith("/api/onboarding/admin");
    }

    public static boolean isRegistrationOrConfirmationPublicPath(String path) {
        if (path == null) {
            return false;
        }
        return isAdminOnboardingPublicPath(path)
                || path.contains("/registro")
                || path.contains("/confirmar")
                || path.contains("/reenviar-confirmacion");
    }

    public static boolean isPublicReferenceDataGet(String path, String method) {
        if (!"GET".equalsIgnoreCase(method)) {
            return false;
        }
        return path.contains("/api/provincias")
                || path.contains("/api/localidades")
                || path.contains("/api/direcciones")
                || path.contains("/api/especialidades")
                || path.contains("/api/roles")
                || path.contains("/api/estados")
                || path.contains("/api/obras-sociales");
    }

    public static boolean isPublicProfesionalCatalogGet(String path, String method) {
        if (!"GET".equalsIgnoreCase(method) || path == null) {
            return false;
        }
        if ("/api/profesionales/presentacion".equals(path)) {
            return true;
        }
        if (path.matches("/api/profesionales/\\d+/presentacion")) {
            return true;
        }
        return path.startsWith("/api/profesionales/fotos/public/");
    }

    public static boolean shouldBypassJwtFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = normalizedPath(request);
        return isAuthPublicPath(path)
                || isRegistrationOrConfirmationPublicPath(path)
                || isPublicReferenceDataGet(path, request.getMethod())
                || isPublicProfesionalCatalogGet(path, request.getMethod());
    }
}
