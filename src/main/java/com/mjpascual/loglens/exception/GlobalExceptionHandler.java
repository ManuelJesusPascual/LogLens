package com.mjpascual.loglens.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;


/**
 * Global exception handler for the loglens API.
 * <p>
 * This class intercepts exceptions thrown by REST controllers and transforms them
 * into standardized JSON error responses. It ensures that all unhandled exceptions
 * are caught and returned with appropriate HTTP status codes and error messages.
 *
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * This method serves as a catch-all handler for any exception that is not
     * specifically handled elsewhere. It returns a standardized error response
     * containing the exception message.
     *
     * @param ex the exception that was thrown
     * @return a ResponseEntity containing a map with the error message and HTTP 400 status
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleAll(Exception ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("error", "Unexpected Error: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resp);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorized(UnauthorizedException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(resp);
    }

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<Map<String, String>> handleTooManyRequests(TooManyRequestsException ex) {
        Map<String, String> resp = new HashMap<>();
        resp.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(resp);
    }
}
