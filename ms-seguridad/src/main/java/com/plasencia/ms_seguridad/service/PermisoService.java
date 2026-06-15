package com.plasencia.ms_seguridad.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.plasencia.ms_seguridad.dto.PermisoRequest;
import com.plasencia.ms_seguridad.dto.PermisoResponse;
import com.plasencia.ms_seguridad.entity.Permiso;
import com.plasencia.ms_seguridad.exception.RecursoDuplicadoException;
import com.plasencia.ms_seguridad.exception.RecursoNoEncontradoException;
import com.plasencia.ms_seguridad.repository.PermisoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PermisoService {

    private final PermisoRepository permisoRepository;

    @Transactional(readOnly = true)
    public List<PermisoResponse> listar() {
        return permisoRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional
    public PermisoResponse crear(PermisoRequest request) {
        if (permisoRepository.existsByNombre(request.nombre())) {
            throw new RecursoDuplicadoException("Ya existe el permiso: " + request.nombre());
        }
        Permiso permiso = Permiso.builder()
                .nombre(request.nombre())
                .descripcion(request.descripcion())
                .build();
        return toResponse(permisoRepository.save(permiso));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!permisoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el permiso con id: " + id);
        }
        permisoRepository.deleteById(id);
    }

    private PermisoResponse toResponse(Permiso p) {
        return new PermisoResponse(p.getId(), p.getNombre(), p.getDescripcion());
    }
}
