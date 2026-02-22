package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.dto.CorrectionResultResponse;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.CorrectionResultRepository;
import com.projeto.examcorrection.repository.ExamReportRepository;
import com.projeto.examcorrection.repository.ExamStatisticsRepository;
import com.projeto.examcorrection.repository.SubmissionRepository;
import com.projeto.examcorrection.repository.AnswerKeyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CorrectionService {

    private static final Logger log = LoggerFactory.getLogger(CorrectionService.class);

    private final SubmissionRepository submissionRepository;
    private final CorrectionResultRepository correctionResultRepository;
    private final AnswerKeyRepository answerKeyRepository;
    private final ExamService examService;
    private final ExamReportRepository examReportRepository;
    private final ExamStatisticsRepository examStatisticsRepository;

    public CorrectionService(SubmissionRepository submissionRepository,
            CorrectionResultRepository correctionResultRepository,
            AnswerKeyRepository answerKeyRepository,
            ExamService examService,
            ExamReportRepository examReportRepository,
            ExamStatisticsRepository examStatisticsRepository) {
        this.submissionRepository = submissionRepository;
        this.correctionResultRepository = correctionResultRepository;
        this.answerKeyRepository = answerKeyRepository;
        this.examService = examService;
        this.examReportRepository = examReportRepository;
        this.examStatisticsRepository = examStatisticsRepository;
    }

    public CorrectionResultResponse correct(String submissionId, String userId, Role role) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("SUBMISSION_NOT_FOUND", "Submissão não encontrada."));

        Exam exam = examService.findById(sub.getExamId());
        if (role == Role.PROFESSOR && !exam.getProfessorId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
        }

        if (correctionResultRepository.existsBySubmissionId(submissionId)) {
            throw new BusinessRuleException("ALREADY_CORRECTED", "Esta submissão já foi corrigida.");
        }

        AnswerKey answerKey = answerKeyRepository.findByExamId(sub.getExamId())
                .orElseThrow(() -> new BusinessRuleException("ANSWER_KEY_NOT_FOUND",
                        "Gabarito não encontrado para correção."));

        Map<String, String> gabarito = answerKey.getRespostas();
        Map<String, String> respostasAluno = sub.getRespostas();

        int acertos = 0;
        int erros = 0;
        double notaFinal = 0.0;
        List<QuestionDetail> detalhes = new ArrayList<>();

        for (Question q : exam.getQuestions()) {
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
        result = correctionResultRepository.save(result);

        sub.setCorrigida(true);
        sub.setNota(notaFinal);
        submissionRepository.save(sub);

        // Invalidate cached reports/stats
        examReportRepository.deleteByExamId(sub.getExamId());
        examStatisticsRepository.deleteByExamId(sub.getExamId());

        log.info("Submission corrected: id={}, nota={}", submissionId, notaFinal);
        return toResponse(result);
    }

    public CorrectionResultResponse getResult(String submissionId, String userId, Role role) {
        Submission sub = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("SUBMISSION_NOT_FOUND", "Submissão não encontrada."));

        if (role == Role.ALUNO && !sub.getAlunoId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
        }
        if (role == Role.PROFESSOR) {
            Exam exam = examService.findById(sub.getExamId());
            if (!exam.getProfessorId().equals(userId)) {
                throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
            }
        }

        CorrectionResult result = correctionResultRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException("CORRECTION_NOT_FOUND",
                        "Resultado de correção não encontrado."));
        return toResponse(result);
    }

    private CorrectionResultResponse toResponse(CorrectionResult r) {
        return new CorrectionResultResponse(r.getId(), r.getSubmissionId(), r.getAcertos(), r.getErros(),
                r.getNotaFinal(), r.getDetalhesPorQuestao());
    }
}
