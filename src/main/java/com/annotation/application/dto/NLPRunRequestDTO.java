package com.annotation.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * DTO carrying parameters used to launch a training or test NLP run.
 */
public record NLPRunRequestDTO(
        @NotNull Long datasetId,
        @DecimalMin("0.0001") Double learningRate,
        @Min(1) @Max(200) Integer epochs,
        @Min(1) Integer batchSize,
        @NotBlank String scriptPath
) {
}
