package com.projeto.examcorrection.dto;

import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record SubmissionRequest(
        @NotNull Map<String, String> respostas // questionId -> alternativaSelecionada
) {
}
