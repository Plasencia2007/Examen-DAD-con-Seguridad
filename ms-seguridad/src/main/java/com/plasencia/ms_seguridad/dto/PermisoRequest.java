package com.plasencia.ms_seguridad.dto;

import jakarta.validation.constraints.NotBlank;

public record PermisoRequest(
        @NotBlank(message = "El nombre del permiso es obligatorio") String nombre,
        String descripcion) {
}
