package com.plasencia.ms_reserva.dto;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de salida con los 6 campos de la reserva.
 */
public record ReservaResponse(
        String nroReser,
        LocalDate fechaReser,
        LocalTime horaReser,
        String codCli,
        Integer idProg,
        String codDest
) {
}
