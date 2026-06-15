package com.plasencia.ms_seguridad.dto;

import java.time.Instant;
import java.util.List;

/** Respuesta del login: el token JWT y datos &#250;tiles para el cliente. */
public record LoginResponse(
        String accessToken,
        String tokenType,
        Instant expiraEn,
        String username,
        List<String> roles,
        List<String> permisos) {
}
