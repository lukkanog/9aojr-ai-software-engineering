package com.projeto.examcorrection.error;

public record ErrorResponse(
        String code,
        String message,
        Object details,
        String traceId) {
}
