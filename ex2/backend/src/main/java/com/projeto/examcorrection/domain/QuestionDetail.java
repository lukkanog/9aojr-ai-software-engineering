package com.projeto.examcorrection.domain;

public record QuestionDetail(
        String questionId,
        boolean correta,
        String respostaAluno,
        String respostaEsperada,
        double pontuacaoObtida) {
}
