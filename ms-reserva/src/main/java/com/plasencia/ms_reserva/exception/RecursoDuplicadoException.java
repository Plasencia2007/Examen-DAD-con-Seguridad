package com.plasencia.ms_reserva.exception;

/**
 * Se lanza cuando se intenta crear un recurso que ya existe. Mapea a HTTP 409.
 */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
