package com.annotation.application.dto;

/**
 * DTO returned by the AJAX polling endpoint for NLP run progress.
 */
public record NLPRunLogDTO(
        String logs,
        String status,
        MetricsResultDTO metrics
) {
}
