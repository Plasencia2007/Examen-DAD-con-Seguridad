package com.plasencia.ms_seguridad.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.plasencia.ms_seguridad.entity.Permiso;
import com.plasencia.ms_seguridad.entity.Rol;
import com.plasencia.ms_seguridad.entity.Usuario;
import com.plasencia.ms_seguridad.repository.PermisoRepository;
import com.plasencia.ms_seguridad.repository.RolRepository;
import com.plasencia.ms_seguridad.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Carga datos iniciales la primera vez (idempotente):
 * <ul>
 *   <li>Permisos de reserva: LEER, CREAR, ACTUALIZAR, ELIMINAR</li>
 *   <li>Roles: ADMIN (todo), OPERADOR (leer/crear/actualizar), USER (solo leer)</li>
 *   <li>Usuario admin/admin123 con rol ADMIN para arrancar la gesti&#243;n</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        Permiso leer = permiso("RESERVA_LEER", "Consultar reservas");
        Permiso crear = permiso("RESERVA_CREAR", "Registrar reservas");
        Permiso actualizar = permiso("RESERVA_ACTUALIZAR", "Modificar reservas");
        Permiso eliminar = permiso("RESERVA_ELIMINAR", "Eliminar reservas");

        rol("ADMIN", "Acceso total", new HashSet<>(Arrays.asList(leer, crear, actualizar, eliminar)));
        rol("OPERADOR", "Gestiona reservas sin eliminarlas",
                new HashSet<>(Arrays.asList(leer, crear, actualizar)));
        rol("USER", "Solo lectura de reservas", new HashSet<>(List.of(leer)));

        usuarioAdmin();
    }

    private Permiso permiso(String nombre, String descripcion) {
        return permisoRepository.findByNombre(nombre)
                .orElseGet(() -> permisoRepository.save(
                        Permiso.builder().nombre(nombre).descripcion(descripcion).build()));
    }

    private void rol(String nombre, String descripcion, Set<Permiso> permisos) {
        if (rolRepository.existsByNombre(nombre)) {
            return;
        }
        rolRepository.save(Rol.builder()
                .nombre(nombre)
                .descripcion(descripcion)
                .permisos(permisos)
                .build());
        log.info("Rol creado: {}", nombre);
    }

    private void usuarioAdmin() {
        if (usuarioRepository.existsByUsername("admin")) {
            return;
        }
        Rol admin = rolRepository.findByNombre("ADMIN").orElseThrow();
        usuarioRepository.save(Usuario.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .nombreCompleto("Administrador del sistema")
                .enabled(true)
                .rol(admin)
                .build());
        log.info("Usuario inicial creado -> admin / admin123 (rol ADMIN)");
    }
}
