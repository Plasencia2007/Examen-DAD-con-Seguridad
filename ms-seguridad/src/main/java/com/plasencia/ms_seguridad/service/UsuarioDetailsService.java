package com.plasencia.ms_seguridad.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.plasencia.ms_seguridad.entity.Rol;
import com.plasencia.ms_seguridad.entity.Usuario;
import com.plasencia.ms_seguridad.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

/**
 * Carga el usuario desde la base de datos para que Spring Security valide
 * sus credenciales durante el login. Expone su rol ({@code ROLE_*}) y los
 * permisos de ese rol como authorities.
 */
@Service
@RequiredArgsConstructor
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

        Rol rol = usuario.getRol();
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
        rol.getPermisos().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.getNombre())));

        return User.builder()
                .username(usuario.getUsername())
                .password(usuario.getPassword())
                .authorities(authorities)
                .disabled(!usuario.isEnabled())
                .build();
    }
}
