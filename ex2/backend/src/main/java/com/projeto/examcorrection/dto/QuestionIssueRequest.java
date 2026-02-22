package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.Severidade;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record QuestionIssueRequest(
        @NotBlank String tipoProblema,
        @NotNull Severidade severidade,
        @NotBlank String descricao) {
}
