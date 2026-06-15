package com.plasencia.ms_reserva.controller;

import com.plasencia.ms_reserva.dto.ReservaRequest;
import com.plasencia.ms_reserva.dto.ReservaResponse;
import com.plasencia.ms_reserva.service.ReservaService;
import jakarta.validation.Valid;
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

import java.util.List;

/**
 * API REST de reservas. La autorización se controla por permiso en cada endpoint.
 */
@RestController
@RequestMapping("/reservas")
public class ReservaController {

    private final ReservaService service;

    public ReservaController(ReservaService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('RESERVA_LEER')")
    public List<ReservaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/{nro}")
    @PreAuthorize("hasAuthority('RESERVA_LEER')")
    public ReservaResponse obtener(@PathVariable String nro) {
        return service.obtener(nro);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RESERVA_CREAR')")
    public ReservaResponse crear(@Valid @RequestBody ReservaRequest req) {
        return service.crear(req);
    }

    @PutMapping("/{nro}")
    @PreAuthorize("hasAuthority('RESERVA_ACTUALIZAR')")
    public ReservaResponse actualizar(@PathVariable String nro,
                                      @Valid @RequestBody ReservaRequest req) {
        return service.actualizar(nro, req);
    }

    @DeleteMapping("/{nro}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('RESERVA_ELIMINAR')")
    public ResponseEntity<Void> eliminar(@PathVariable String nro) {
        service.eliminar(nro);
        return ResponseEntity.noContent().build();
    }
}
