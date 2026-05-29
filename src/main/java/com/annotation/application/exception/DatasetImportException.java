package com.annotation.application.exception;

/**
 * Exception thrown when a dataset file fails to import or parse.
 */
public class DatasetImportException extends RuntimeException {
    
    public DatasetImportException(String message) {
        super(message);
    }
    
    public DatasetImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
