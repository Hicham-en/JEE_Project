package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for Text Pair transfer.
 */
public record TextPairDTO(
    Long id,

    @NotBlank(message = "Text 1 is required")
    String text1,

    String text2,

    String metadata
) {}
