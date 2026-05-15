package com.splitup.service;

/**
 * Excepción de dominio para violaciones de reglas de negocio.
 * Se distingue de RuntimeException para que los servicios puedan lanzarla
 * y los clientes (UI, API) puedan capturarla y mostrar mensajes útiles al usuario.
 */
public class BusinessException extends Exception {

    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
