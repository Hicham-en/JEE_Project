package com.annotation.application.dto;

import java.util.List;

/**
 * DTO representing a potentially spamming annotator.
 */
public record SpammerDTO(
    Long annotatorId,
    String nom,
    String prenom,
    Double suspicionScore,
    List<String> raisons
) {}
