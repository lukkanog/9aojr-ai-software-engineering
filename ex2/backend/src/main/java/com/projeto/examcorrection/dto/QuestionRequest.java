package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.QuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record QuestionRequest(
        @NotBlank String enunciado,
        @NotNull QuestionType tipo,
        @NotNull @Size(min = 2) List<String> alternativas,
        @Positive double pontuacao,
        @Positive int ordem) {
}
