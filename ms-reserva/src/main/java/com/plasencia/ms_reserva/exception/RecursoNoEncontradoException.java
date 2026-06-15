package com.plasencia.ms_reserva.exception;

/**
 * Se lanza cuando el recurso solicitado no existe. Mapea a HTTP 404.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
