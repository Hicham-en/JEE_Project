package com.annotation.application.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * DTO for Task transfer.
 */
public record TaskDTO(
    Long id,
    
    @NotNull(message = "Dataset ID is required")
    Long datasetId,
    
    String datasetName,
    
    @NotNull(message = "Annotator ID is required")
    Long annotatorId,
    
    LocalDateTime dateLimit,
    String status,
    Integer nbTotal,
    Integer nbAnnotated,
    Double progressPercent
) {}
