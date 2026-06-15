package com.plasencia.ms_seguridad.dto;

import java.util.List;

public record RolResponse(Long id, String nombre, String descripcion, List<String> permisos) {
}
