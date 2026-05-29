package com.annotation.application.dto;

/**
 * DTO containing metrics parsed from NLP script logs.
 */
public record MetricsResultDTO(
        Double accuracy,
        Double f1Score,
        String confusionMatrix
) {
}
