package com.projeto.examcorrection.dto;

import java.time.Instant;
import java.util.Map;

public record AnswerKeyResponse(
        String id,
        String examId,
        Map<String, String> respostas,
        Instant dataCriacao,
        Instant dataAtualizacao) {
}
