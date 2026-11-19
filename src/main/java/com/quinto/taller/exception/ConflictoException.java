package com.quinto.taller.exception;

/** Se traduce a HTTP 409 (registro duplicado, regla de negocio que impide la acción). */
public class ConflictoException extends RuntimeException {
    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
