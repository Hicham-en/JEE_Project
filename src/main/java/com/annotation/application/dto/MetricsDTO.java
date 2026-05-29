package com.annotation.application.dto;

/**
 * DTO for system metrics.
 */
public record MetricsDTO(
    Long datasetId,
    Double fleissKappa,
    Integer nbAnnotateursActifs,
    Integer nbTextesEvalues,
    String interpretation
) {}
