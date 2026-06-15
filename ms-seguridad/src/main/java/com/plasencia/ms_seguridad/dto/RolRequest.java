package com.plasencia.ms_seguridad.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record RolRequest(
        @NotBlank(message = "El nombre del rol es obligatorio") String nombre,
        String descripcion,
        List<String> permisos) {
}
