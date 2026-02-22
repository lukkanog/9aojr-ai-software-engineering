package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.QuestionDetail;
import java.util.List;

public record CorrectionResultResponse(
        String id,
        String submissionId,
        int acertos,
        int erros,
        double notaFinal,
        List<QuestionDetail> detalhesPorQuestao) {
}
