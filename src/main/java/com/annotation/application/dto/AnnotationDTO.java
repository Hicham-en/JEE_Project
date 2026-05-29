package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for Annotation transfer.
 */
public record AnnotationDTO(
    Long id,
    
    @NotNull(message = "Task ID is required")
    Long taskId,
    
    @NotNull(message = "Text Pair ID is required")
    Long textPairId,
    
    @NotBlank(message = "Chosen class cannot be blank")
    String chosenClass,
    
    LocalDateTime annotationTime,
    String source
) {}
