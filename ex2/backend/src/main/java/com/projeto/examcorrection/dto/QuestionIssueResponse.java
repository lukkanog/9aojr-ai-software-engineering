package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.GeradoPor;
import com.projeto.examcorrection.domain.Severidade;
import java.time.Instant;

public record QuestionIssueResponse(
        String id,
        String questionId,
        String examId,
        String tipoProblema,
        Severidade severidade,
        String descricao,
        GeradoPor geradoPor,
        Instant dataIdentificacao) {
}
