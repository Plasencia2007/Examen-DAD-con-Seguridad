package com.plasencia.ms_seguridad.exception;

/** Se lanza cuando se intenta crear un recurso que ya existe (devuelve 409). */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
