package com.projeto.examcorrection.dto;

import jakarta.validation.constraints.NotEmpty;
import java.util.Map;

public record AnswerKeyRequest(
        @NotEmpty Map<String, String> respostas // questionId -> alternativaCorreta
) {
}
