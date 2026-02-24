package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.Role;

public record AuthResponse(
        String token,
        String userId,
        String nome,
        String email,
        Role role) {
}
