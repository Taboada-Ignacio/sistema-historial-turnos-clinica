package com.clinica.usuarios.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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
            path.contains("/confirmar") ||
            path.contains("/reenviar-confirmacion")) {
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
            handleException(
                    response,
                    "El tiempo de sesión ha caducado. Por favor, inicie sesión nuevamente.",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_EXPIRED");
        } catch (JwtException e) {
            handleException(
                    response,
                    "Token inválido o firma incorrecta.",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_INVALID");
        } catch (Exception e) {
            handleException(
                    response,
                    "Error de autenticación.",
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "TOKEN_INVALID");
        }
    }

    private void handleException(HttpServletResponse response, String message, int status, String code)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        String safeMessage = jsonEscape(message);
        String jsonResponse = String.format(
                "{\"status\": %d, \"error\": \"Unauthorized\", \"message\": \"%s\", \"code\": \"%s\"}",
                status, safeMessage, jsonEscape(code));
        response.getWriter().write(jsonResponse);
    }

    private static String jsonEscape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", " ").replace("\n", " ");
    }
}