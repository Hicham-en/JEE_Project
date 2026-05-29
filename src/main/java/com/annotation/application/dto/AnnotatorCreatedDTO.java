package com.annotation.application.dto;

public record AnnotatorCreatedDTO(
    AnnotatorDTO annotator,
    String generatedPassword
) {}
