package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO for User transfer.
 */
public record UserDTO(
    Long id,
    @NotBlank(message = "Login is required")
    @Size(min = 3, max = 50, message = "Login must be between 3 and 50 characters")
    String login,
    
    @NotBlank(message = "First name is required")
    String prenom,
    
    @NotBlank(message = "Last name is required")
    String nom,
    
    boolean active
) {}
