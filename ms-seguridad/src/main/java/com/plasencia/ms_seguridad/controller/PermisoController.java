package com.plasencia.ms_seguridad.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plasencia.ms_seguridad.dto.PermisoRequest;
import com.plasencia.ms_seguridad.dto.PermisoResponse;
import com.plasencia.ms_seguridad.service.PermisoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/seguridad/permisos")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PermisoController {

    private final PermisoService permisoService;

    @GetMapping
    public List<PermisoResponse> listar() {
        return permisoService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermisoResponse crear(@Valid @RequestBody PermisoRequest request) {
        return permisoService.crear(request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        permisoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
