package com.plasencia.ms_seguridad.dto;

import jakarta.validation.constraints.NotBlank;

/** Body para cambiar el rol de un usuario: {"rol":"USER"} */
public record CambiarRolRequest(
        @NotBlank(message = "El rol es obligatorio") String rol) {
}
