package com.projeto.examcorrection.dto;

import java.time.Instant;
import java.util.Map;

public record SubmissionResponse(
        String id,
        String examId,
        String alunoId,
        String alunoNome,
        Map<String, String> respostas,
        Double nota,
        boolean corrigida,
        Instant dataEnvio) {
}

