package com.plasencia.ms_reserva.service;

import com.plasencia.ms_reserva.dto.ReservaRequest;
import com.plasencia.ms_reserva.dto.ReservaResponse;
import com.plasencia.ms_reserva.entity.Reserva;
import com.plasencia.ms_reserva.exception.RecursoDuplicadoException;
import com.plasencia.ms_reserva.exception.RecursoNoEncontradoException;
import com.plasencia.ms_reserva.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Lógica de negocio de reservas. Aplica las reglas de existencia/duplicidad
 * y realiza el mapeo interno entre entidad y DTOs.
 */
@Service
@Transactional
public class ReservaService {

    private final ReservaRepository repository;

    public ReservaService(ReservaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ReservaResponse> listar() {
        return repository.findAll().stream()
                .map(this::aResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ReservaResponse obtener(String nro) {
        Reserva reserva = repository.findById(nro)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la reserva con nro: " + nro));
        return aResponse(reserva);
    }

    public ReservaResponse crear(ReservaRequest req) {
        if (repository.existsById(req.nroReser())) {
            throw new RecursoDuplicadoException(
                    "Ya existe una reserva con nro: " + req.nroReser());
        }
        Reserva guardada = repository.save(aEntidad(req));
        return aResponse(guardada);
    }

    public ReservaResponse actualizar(String nro, ReservaRequest req) {
        Reserva reserva = repository.findById(nro)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la reserva con nro: " + nro));
        // La PK no se modifica; se actualizan los demás campos.
        reserva.setFechaReser(req.fechaReser());
        reserva.setHoraReser(req.horaReser());
        reserva.setCodCli(req.codCli());
        reserva.setIdProg(req.idProg());
        reserva.setCodDest(req.codDest());
        return aResponse(repository.save(reserva));
    }

    public void eliminar(String nro) {
        if (!repository.existsById(nro)) {
            throw new RecursoNoEncontradoException(
                    "No existe la reserva con nro: " + nro);
        }
        repository.deleteById(nro);
    }

    // ---- Mapeo interno ----

    private Reserva aEntidad(ReservaRequest req) {
        return Reserva.builder()
                .nroReser(req.nroReser())
                .fechaReser(req.fechaReser())
                .horaReser(req.horaReser())
                .codCli(req.codCli())
                .idProg(req.idProg())
                .codDest(req.codDest())
                .build();
    }

    private ReservaResponse aResponse(Reserva r) {
        return new ReservaResponse(
                r.getNroReser(),
                r.getFechaReser(),
                r.getHoraReser(),
                r.getCodCli(),
                r.getIdProg(),
                r.getCodDest());
    }
}
