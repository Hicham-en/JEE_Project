package com.annotation.application.exception;

/**
 * Exception thrown when a requested entity cannot be found.
 */
public class EntityNotFoundException extends RuntimeException {
    
    /**
     * Constructs a new EntityNotFoundException with the specified detail message.
     *
     * @param message the detail message
     */
    public EntityNotFoundException(String message) {
        super(message);
    }
}
