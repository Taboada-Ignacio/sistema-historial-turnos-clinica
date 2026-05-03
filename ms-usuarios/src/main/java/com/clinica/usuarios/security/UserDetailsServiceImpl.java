package com.clinica.usuarios.security;

import com.clinica.usuarios.model.Usuario;
import com.clinica.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
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

        Collection<? extends GrantedAuthority> authorities = usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getDescripcion()))
                .collect(Collectors.toList());

        // Mejora: Verificamos que el objeto estado y su nombre no sean nulos
        boolean enabled = usuario.getEstadoActual() != null && 
                         "ACTIVO".equalsIgnoreCase(usuario.getEstadoActual().getNombre());

        return new org.springframework.security.core.userdetails.User(
                usuario.getEmail(),
                usuario.getPassword(),
                enabled, // Si es false, Spring Security lanza DisabledException
                true,    // accountNonExpired
                true,    // credentialsNonExpired
                true,    // accountNonLocked
                authorities
        );
    }
}