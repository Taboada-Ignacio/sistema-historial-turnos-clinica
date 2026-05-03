package com.clinica.usuarios.security;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getServletPath();
        String method = request.getMethod();

        // 1. Siempre ignorar peticiones OPTIONS (Pre-flight de CORS)
        if (request.getMethod().equals(HttpMethod.OPTIONS.name())) {
            return true;
        }

        // 2. Rutas de Auth, Registro y Confirmación (Cualquier método)
        if (path.contains("/api/auth/") || 
            path.contains("/registro") || 
            path.contains("/confirmar")) {
            return true;
        }

        // 3. Datos de referencia públicos (Solo si es GET)
        // Esto asegura que si alguien intenta un POST a provincias, el filtro SÍ actúe
        if (request.getMethod().equals(HttpMethod.GET.name())) {
            return path.contains("/api/provincias") || 
                path.contains("/api/localidades") || 
                path.contains("/api/especialidades") || 
                path.contains("/api/roles") || 
                path.contains("/api/obras-sociales");
        }

        return false;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtUtil.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtUtil.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            // MEJORA 2: Manejo específico de token expirado para el Frontend
            handleException(response, "El tiempo de sesión ha caducado. Por favor, inicie sesión nuevamente.", 401);
        } catch (Exception e) {
            // Otros errores de seguridad (token mal formado, etc.)
            handleException(response, "Error de autenticación: " + e.getMessage(), 403);
        }
    }

    private void handleException(HttpServletResponse response, String message, int status) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        // Escribimos un JSON que el front pueda leer fácilmente
        String jsonResponse = String.format(
            "{\"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\", \"code\": \"TOKEN_EXPIRED\"}", 
            status, message
        );
        response.getWriter().write(jsonResponse);
    }
}