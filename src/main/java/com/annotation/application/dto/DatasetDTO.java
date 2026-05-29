package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;

/**
 * DTO for Dataset transfer.
 */
public record DatasetDTO(
    Long id,
    
    @NotBlank(message = "Name is required")
    String nom,
    
    String description,
    
    @NotBlank(message = "Language is required")
    String langue,
    
    LocalDateTime createdAt,
    Double progressPercent
) {}
