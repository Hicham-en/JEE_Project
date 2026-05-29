package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for creating a new Dataset.
 */
public record DatasetCreateDTO(
    @NotBlank(message = "Name is required")
    String nom,
    
    String description,
    
    @NotBlank(message = "Language is required")
    String langue,
    
    @NotBlank(message = "Must provide at least one possible class context")
    String classes
) {}
