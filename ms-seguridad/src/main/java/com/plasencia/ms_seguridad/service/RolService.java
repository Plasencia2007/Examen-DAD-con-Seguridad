package com.plasencia.ms_seguridad.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plasencia.ms_seguridad.dto.RolRequest;
import com.plasencia.ms_seguridad.dto.RolResponse;
import com.plasencia.ms_seguridad.entity.Permiso;
import com.plasencia.ms_seguridad.entity.Rol;
import com.plasencia.ms_seguridad.exception.RecursoDuplicadoException;
import com.plasencia.ms_seguridad.exception.RecursoNoEncontradoException;
import com.plasencia.ms_seguridad.repository.PermisoRepository;
import com.plasencia.ms_seguridad.repository.RolRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    @Transactional(readOnly = true)
    public List<RolResponse> listar() {
        return rolRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public RolResponse crear(RolRequest request) {
        if (rolRepository.existsByNombre(request.nombre())) {
            throw new RecursoDuplicadoException("Ya existe el rol: " + request.nombre());
        }
        Rol rol = Rol.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .permisos(resolverPermisos(request.permisos()))
                .build();
        return toResponse(rolRepository.save(rol));
    }

    @Transactional
    public RolResponse actualizarPermisos(Long id, List<String> permisos) {
        Rol rol = rolRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el rol con id: " + id));
        rol.setPermisos(resolverPermisos(permisos));
        return toResponse(rolRepository.save(rol));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!rolRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el rol con id: " + id);
        }
        rolRepository.deleteById(id);
    }

    /** Resuelve los permisos por nombre; falla si alguno no existe. */
    private Set<Permiso> resolverPermisos(List<String> nombres) {
        Set<Permiso> permisos = new HashSet<>();
        if (nombres == null) {
            return permisos;
        }
        for (String nombre : nombres) {
            Permiso permiso = permisoRepository.findByNombre(nombre)
                    .orElseThrow(() -> new RecursoNoEncontradoException("No existe el permiso: " + nombre));
            permisos.add(permiso);
        }
        return permisos;
    }

    private RolResponse toResponse(Rol rol) {
        List<String> permisos = rol.getPermisos().stream().map(Permiso::getNombre).sorted().toList();
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion(), permisos);
    }
}
