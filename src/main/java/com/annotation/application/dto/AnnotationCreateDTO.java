package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO for creating an Annotation.
 */
public record AnnotationCreateDTO(
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotNull(message = "Text Pair ID is required")
    Long textPairId,
    
    @NotBlank(message = "Class choice must be provided")
    String chosenClass
) {}
