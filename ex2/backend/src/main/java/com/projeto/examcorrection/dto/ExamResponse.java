package com.projeto.examcorrection.dto;

import com.projeto.examcorrection.domain.ExamStatus;
import com.projeto.examcorrection.domain.Question;

import java.time.Instant;
import java.util.List;

public record ExamResponse(
        String id,
        String titulo,
        String descricao,
        String professorId,
        Instant dataInicio,
        Instant dataFim,
        ExamStatus status,
        List<Question> questions,
        Instant dataCriacao) {
}
