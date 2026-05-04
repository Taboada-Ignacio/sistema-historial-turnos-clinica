package com.clinica.usuarios.security;

import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException; // Importante
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con el email: " + email));

        // 1. Obtenemos el nombre del estado de forma segura
        String nombreEstado = (usuario.getEstadoActual() != null) 
                ? usuario.getEstadoActual().getNombre() 
                : "DESCONOCIDO";

        // 2. Validación específica para PENDIENTE
        if ("PENDIENTE".equalsIgnoreCase(nombreEstado)) {
            throw new DisabledException("Su cuenta aún no ha sido activada. Por favor, confirme su correo electrónico para ingresar.");
        }

        // 3. Mapeo de roles
        Collection<? extends GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getDescripcion()))
                .collect(Collectors.toList());

        // 4. Determinamos estados booleanos para el objeto User de Spring
        boolean enabled = "ACTIVO".equalsIgnoreCase(nombreEstado);
        boolean accountNonLocked = !"BLOQUEADO".equalsIgnoreCase(nombreEstado);

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                enabled,           // isEnabled
                true,              // accountNonExpired
                true,              // credentialsNonExpired
                accountNonLocked,  // accountNonLocked
                authorities
        );
    }
}