package com.plasencia.ms_seguridad.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.plasencia.ms_seguridad.dto.RolRequest;
import com.plasencia.ms_seguridad.dto.RolResponse;
import com.plasencia.ms_seguridad.service.RolService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/seguridad/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RolController {

    private final RolService rolService;

    @GetMapping
    public List<RolResponse> listar() {
        return rolService.listar();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RolResponse crear(@Valid @RequestBody RolRequest request) {
        return rolService.crear(request);
    }

    /** Reemplaza la lista de permisos del rol. Body: ["RESERVA_LEER","RESERVA_CREAR"] */
    @PutMapping("/{id}/permisos")
    public RolResponse actualizarPermisos(@PathVariable Long id, @RequestBody List<String> permisos) {
        return rolService.actualizarPermisos(id, permisos);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        rolService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
