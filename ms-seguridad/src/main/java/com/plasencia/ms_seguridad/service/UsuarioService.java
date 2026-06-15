package com.plasencia.ms_seguridad.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plasencia.ms_seguridad.dto.UsuarioRequest;
import com.plasencia.ms_seguridad.dto.UsuarioResponse;
import com.plasencia.ms_seguridad.entity.Permiso;
import com.plasencia.ms_seguridad.entity.Rol;
import com.plasencia.ms_seguridad.entity.Usuario;
import com.plasencia.ms_seguridad.exception.RecursoDuplicadoException;
import com.plasencia.ms_seguridad.exception.RecursoNoEncontradoException;
import com.plasencia.ms_seguridad.repository.RolRepository;
import com.plasencia.ms_seguridad.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    @Transactional
    public UsuarioResponse crear(UsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw new RecursoDuplicadoException("Ya existe el usuario: " + request.username());
        }
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nombreCompleto(request.nombreCompleto())
                .enabled(true)
                .rol(resolverRol(request.rol()))
                .build();
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizarRol(Long id, String rol) {
        Usuario usuario = buscar(id);
        usuario.setRol(resolverRol(rol));
        return toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el usuario con id: " + id);
        }
        usuarioRepository.deleteById(id);
    }

    private Usuario buscar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el usuario con id: " + id));
    }

    private Rol resolverRol(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol: " + nombre));
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        String rol = usuario.getRol().getNombre();
        List<String> permisos = usuario.getRol().getPermisos().stream()
                .map(Permiso::getNombre)
                .distinct()
                .sorted()
                .toList();
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombreCompleto(),
                usuario.isEnabled(),
                rol,
                permisos);
    }
}
