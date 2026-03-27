package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.CorrectionResult;
import com.projeto.examcorrection.domain.Question;
import com.projeto.examcorrection.domain.QuestionDetail;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CorrectionCalculator {

    public CorrectionResult calculate(Map<String, String> gabarito, Map<String, String> respostasAluno, List<Question> questions, String submissionId) {
        
        List<QuestionDetail> detalhes = questions.stream().map(q -> {
            boolean correta = false;
            double pontuacaoObtida = 0.0;
            String respostaEsperada = gabarito.get(q.getId());
            String respostaAluno = respostasAluno.get(q.getId());

            if (respostaAluno != null && respostaAluno.equals(respostaEsperada)) {
                correta = true;
                pontuacaoObtida = q.getPontuacao();
            }

            return new QuestionDetail(q.getId(), correta, respostaAluno, respostaEsperada, pontuacaoObtida);
        }).toList();

        int acertos = (int) detalhes.stream().filter(QuestionDetail::correta).count();
        int erros = (int) detalhes.stream().filter(d -> !d.correta() && d.respostaAluno() != null).count();
        double notaFinal = detalhes.stream().mapToDouble(QuestionDetail::pontuacaoObtida).sum();

        CorrectionResult result = new CorrectionResult();
        result.setSubmissionId(submissionId);
        result.setAcertos(acertos);
        result.setErros(erros);
        result.setNotaFinal(notaFinal);
        result.setDetalhesPorQuestao(detalhes);

        return result;
    }
}
