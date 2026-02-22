package com.projeto.examcorrection.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record ExamRequest(
        @NotBlank String titulo,
        String descricao,
        Instant dataInicio,
        Instant dataFim) {
}
