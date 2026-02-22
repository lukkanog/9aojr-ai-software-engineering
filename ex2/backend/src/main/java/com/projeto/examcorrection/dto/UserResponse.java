package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.Role;

import java.time.Instant;

public record UserResponse(
        String id,
        String nome,
        String email,
        Role role,
        boolean ativo,
        Instant dataCriacao) {
}
