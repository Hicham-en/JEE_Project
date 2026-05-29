package com.annotation.interfaces.web.advice;

import com.annotation.application.exception.EntityNotFoundException;
import com.annotation.application.exception.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Global Exception Handler to map exceptions to standard HTTP responses and views.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles EntityNotFoundException.
     *
     * @param ex the exception
     * @return view name for 404 error page
     */
    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleEntityNotFound(EntityNotFoundException ex, Model model) {
        log.warn("Entity not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    /**
     * Handles business ValidationException.
     *
     * @param ex the exception
     * @return view name or string response
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationException(ValidationException ex, Model model) {
        log.warn("Validation error: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/400";
    }

    /**
     * Handles Spring MVC bean validation errors (Spring Boot 3 pattern).
     *
     * @param ex the exception
     * @return view name
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleMethodArgumentNotValid(MethodArgumentNotValidException ex, Model model) {
        log.warn("Method argument validation error: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Données fournies invalides.");
        return "error/400";
    }

    /**
     * Handles generic internal server errors.
     *
     * @param ex the exception
     * @return view name for 500 error page
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex) {
        log.error("An unexpected error occurred", ex);
        return "error/500";
    }
}
