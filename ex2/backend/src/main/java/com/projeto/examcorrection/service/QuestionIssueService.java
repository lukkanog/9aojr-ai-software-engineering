package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.GeradoPor;
import com.projeto.examcorrection.domain.QuestionIssue;
import com.projeto.examcorrection.dto.QuestionIssueRequest;
import com.projeto.examcorrection.dto.QuestionIssueResponse;
import com.projeto.examcorrection.repository.QuestionIssueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class QuestionIssueService {

    private static final Logger log = LoggerFactory.getLogger(QuestionIssueService.class);

    private final QuestionIssueRepository questionIssueRepository;
    private final ExamService examService;

    public QuestionIssueService(QuestionIssueRepository questionIssueRepository, ExamService examService) {
        this.questionIssueRepository = questionIssueRepository;
        this.examService = examService;
    }

    public List<QuestionIssueResponse> findByQuestionId(String questionId) {
        return questionIssueRepository.findByQuestionId(questionId).stream()
                .map(this::toResponse)
                .toList();
    }

    public QuestionIssueResponse create(String questionId, QuestionIssueRequest request, String professorId) {
        // Find the exam that contains this question to get the examId
        var exam = examService.findExamByQuestionId(questionId);

        QuestionIssue issue = new QuestionIssue();
        issue.setQuestionId(questionId);
        issue.setExamId(exam.getId());
        issue.setTipoProblema(request.tipoProblema());
        issue.setSeveridade(request.severidade());
        issue.setDescricao(request.descricao());
        issue.setGeradoPor(GeradoPor.PROFESSOR);
        issue.setDataIdentificacao(Instant.now());

        issue = questionIssueRepository.save(issue);
        log.info("QuestionIssue created: id={}, questionId={}, type={}", issue.getId(), questionId,
                request.tipoProblema());
        return toResponse(issue);
    }

    private QuestionIssueResponse toResponse(QuestionIssue issue) {
        return new QuestionIssueResponse(issue.getId(), issue.getQuestionId(), issue.getExamId(),
                issue.getTipoProblema(), issue.getSeveridade(), issue.getDescricao(),
                issue.getGeradoPor(), issue.getDataIdentificacao());
    }
}
