package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.CorrectionResult;
import com.projeto.examcorrection.domain.Question;
import com.projeto.examcorrection.domain.QuestionDetail;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CorrectionCalculator {

    public CorrectionResult calculate(Map<String, String> gabarito, Map<String, String> respostasAluno, List<Question> questions, String submissionId) {
        int acertos = 0;
        int erros = 0;
        double notaFinal = 0.0;
        List<QuestionDetail> detalhes = new ArrayList<>();

        for (Question q : questions) {
            QuestionDetail detail = new QuestionDetail();
            detail.setQuestionId(q.getId());
            detail.setRespostaEsperada(gabarito.get(q.getId()));

            String respostaAluno = respostasAluno.get(q.getId());
            detail.setRespostaAluno(respostaAluno);

            if (respostaAluno != null && respostaAluno.equals(gabarito.get(q.getId()))) {
                detail.setCorreta(true);
                detail.setPontuacaoObtida(q.getPontuacao());
                notaFinal += q.getPontuacao();
                acertos++;
            } else {
                detail.setCorreta(false);
                detail.setPontuacaoObtida(0);
                if (respostaAluno != null) {
                    erros++;
                }
            }
            detalhes.add(detail);
        }

        CorrectionResult result = new CorrectionResult();
        result.setSubmissionId(submissionId);
        result.setAcertos(acertos);
        result.setErros(erros);
        result.setNotaFinal(notaFinal);
        result.setDetalhesPorQuestao(detalhes);

        return result;
    }
}
