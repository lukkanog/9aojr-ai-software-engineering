package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        String senha,
        @NotNull Role role) {
}
