package io.joshuasalcedo.commonlibs.domain;

/**
 * Exception thrown when a request is malformed or invalid.
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }

    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}