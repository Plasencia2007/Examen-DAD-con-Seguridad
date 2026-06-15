package com.plasencia.ms_seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario: nombre, contraseña y UN rol. */
public record UsuarioRequest(
        @NotBlank(message = "El username es obligatorio") String username,
        @NotBlank(message = "La contraseña es obligatoria")
        @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres") String password,
        String nombreCompleto,
        @NotBlank(message = "Debe asignar un rol") String rol) {
}
