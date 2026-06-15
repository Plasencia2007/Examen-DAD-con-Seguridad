package com.plasencia.ms_seguridad.dto;

import java.util.List;

public record UsuarioResponse(
        Long id,
        String username,
        String nombreCompleto,
        boolean enabled,
        String rol,
        List<String> permisos) {
}
