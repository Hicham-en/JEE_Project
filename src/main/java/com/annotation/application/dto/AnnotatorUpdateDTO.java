package com.annotation.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AnnotatorUpdateDTO(
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 2, max = 50, message = "Le nom doit contenir entre {min} et {max} caractères")
    String nom,
    
    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 2, max = 50, message = "Le prénom doit contenir entre {min} et {max} caractères")
    String prenom,
    
    @NotBlank(message = "L'identifiant de connexion est obligatoire")
    @Size(min = 4, max = 20, message = "Le login doit contenir entre {min} et {max} caractères")
    String login
) {}
