package com.annotation.application.dto;

import java.util.List;

/**
 * Generic DTO for handling paginated responses to decouple from Spring Data Page interface in API boundaries.
 */
public record PageResponseDTO<T>(
    List<T> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {}
