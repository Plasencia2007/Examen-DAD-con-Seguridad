package com.plasencia.ms_reserva.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * DTO de entrada para crear/actualizar una reserva, con validaciones Jakarta.
 */
public record ReservaRequest(

        @NotBlank
        @Size(min = 8, max = 8)
        String nroReser,

        @NotNull
        LocalDate fechaReser,

        @NotNull
        LocalTime horaReser,

        @NotBlank
        @Size(max = 5)
        String codCli,

        @NotNull
        Integer idProg,

        @NotBlank
        @Size(max = 4)
        String codDest
) {
}
