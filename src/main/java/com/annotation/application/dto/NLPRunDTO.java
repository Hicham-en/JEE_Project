package com.annotation.application.dto;

import java.time.LocalDateTime;

/**
 * DTO for NLP runs execution.
 */
public record NLPRunDTO(
    Long id,
    Long datasetId,
    String type,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long durationSeconds,
    String scriptPath,
    String params,
    String logs,
    Double accuracy,
    Double f1Score,
    String confusionMatrix
) {}
