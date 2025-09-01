package ar.edu.utn.dds.k3003.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

// IMPORTS de tus excepciones personalizadas:
import ar.edu.utn.dds.k3003.exceptions.domain.pdi.HechoInactivoException;
import ar.edu.utn.dds.k3003.exceptions.domain.pdi.HechoInexistenteException;
import ar.edu.utn.dds.k3003.exceptions.infrastructure.solicitudes.SolicitudesCommunicationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, String>> handleNoSuchElementException(NoSuchElementException e) {
        return build(HttpStatus.NOT_FOUND, "Not Found", e.getMessage());
    }

    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<Map<String, String>> handleInvalidParameterException(InvalidParameterException e) {
        return build(HttpStatus.BAD_REQUEST, "Bad Request", e.getMessage());
    }

    // === Nuevos handlers ===

    @ExceptionHandler(HechoInactivoException.class)
    public ResponseEntity<Map<String, String>> handleHechoInactivo(HechoInactivoException e) {
        return build(HttpStatus.CONFLICT, "Hecho Inactivo", e.getMessage());
    }

    @ExceptionHandler(HechoInexistenteException.class)
    public ResponseEntity<Map<String, String>> handleHechoInexistente(HechoInexistenteException e) {
        return build(HttpStatus.NOT_FOUND, "Hecho Inexistente", e.getMessage());
    }

    @ExceptionHandler(SolicitudesCommunicationException.class)
    public ResponseEntity<Map<String, String>> handleSolicitudesCommunication(SolicitudesCommunicationException e) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "Solicitudes Communication Error", e.getMessage());
    }

    // =======================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGenericException(Exception e) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred");
    }

    // Helper para armar la respuesta homogénea
    private ResponseEntity<Map<String, String>> build(HttpStatus status, String error, String message) {
        Map<String, String> response = new HashMap<>();
        response.put("error", error);
        response.put("message", message);
        return new ResponseEntity<>(response, status);
    }
}
