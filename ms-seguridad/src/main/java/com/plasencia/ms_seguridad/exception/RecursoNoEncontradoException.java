package com.plasencia.ms_seguridad.exception;

/** Se lanza cuando no existe el recurso solicitado (devuelve 404). */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
