package io.joshuasalcedo.commonlibs.domain;

/**
 * Exception thrown when there's a conflict with the current state of the resource.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }

    public ConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}