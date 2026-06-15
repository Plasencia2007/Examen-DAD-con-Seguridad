package com.plasencia.ms_seguridad.dto;

import jakarta.validation.constraints.NotBlank;

/** Credenciales enviadas en el login. */
public record LoginRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria") String password) {
}
