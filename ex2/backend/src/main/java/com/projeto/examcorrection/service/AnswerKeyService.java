package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.AnswerKey;
import com.projeto.examcorrection.domain.Exam;
import com.projeto.examcorrection.dto.AnswerKeyRequest;
import com.projeto.examcorrection.dto.AnswerKeyResponse;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.AnswerKeyRepository;
import com.projeto.examcorrection.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class AnswerKeyService {

    private static final Logger log = LoggerFactory.getLogger(AnswerKeyService.class);

    private final AnswerKeyRepository answerKeyRepository;
    private final ExamService examService;
    private final SubmissionRepository submissionRepository;

    public AnswerKeyService(AnswerKeyRepository answerKeyRepository, ExamService examService,
            SubmissionRepository submissionRepository) {
        this.answerKeyRepository = answerKeyRepository;
        this.examService = examService;
        this.submissionRepository = submissionRepository;
    }

    public AnswerKeyResponse getByExamId(String examId, String professorId) {
        Exam exam = examService.findById(examId);
        checkOwnership(exam, professorId);

        AnswerKey ak = answerKeyRepository.findByExamId(examId)
                .orElseThrow(() -> new ResourceNotFoundException("ANSWER_KEY_NOT_FOUND",
                        "Gabarito não encontrado para esta prova."));
        return toResponse(ak);
    }

    public AnswerKeyResponse create(String examId, AnswerKeyRequest request, String professorId) {
        Exam exam = examService.findById(examId);
        checkOwnership(exam, professorId);

        if (answerKeyRepository.existsByExamId(examId)) {
            throw new BusinessRuleException("ANSWER_KEY_ALREADY_EXISTS",
                    "Já existe um gabarito para esta prova. Use PUT para atualizar.");
        }

        AnswerKey ak = new AnswerKey();
        ak.setExamId(examId);
        ak.setRespostas(request.respostas());
        ak.setDataCriacao(Instant.now());
        ak.setDataAtualizacao(Instant.now());

        ak = answerKeyRepository.save(ak);
        log.info("AnswerKey created for exam {}", examId);
        return toResponse(ak);
    }

    public AnswerKeyResponse update(String examId, AnswerKeyRequest request, String professorId) {
        Exam exam = examService.findById(examId);
        checkOwnership(exam, professorId);

        if (submissionRepository.existsByExamId(examId)) {
            throw new BusinessRuleException("ANSWER_KEY_LOCKED",
                    "O gabarito não pode ser alterado após existirem submissões.");
        }

        AnswerKey ak = answerKeyRepository.findByExamId(examId)
                .orElseThrow(() -> new ResourceNotFoundException("ANSWER_KEY_NOT_FOUND",
                        "Gabarito não encontrado para esta prova."));

        ak.setRespostas(request.respostas());
        ak.setDataAtualizacao(Instant.now());
        ak = answerKeyRepository.save(ak);
        log.info("AnswerKey updated for exam {}", examId);
        return toResponse(ak);
    }

    private void checkOwnership(Exam exam, String professorId) {
        if (!exam.getProfessorId().equals(professorId)) {
            throw new BusinessRuleException("ACCESS_DENIED",
                    "Você não tem permissão para acessar o gabarito desta prova.",
                    org.springframework.http.HttpStatus.FORBIDDEN);
        }
    }

    private AnswerKeyResponse toResponse(AnswerKey ak) {
        return new AnswerKeyResponse(ak.getId(), ak.getExamId(), ak.getRespostas(), ak.getDataCriacao(),
                ak.getDataAtualizacao());
    }
}
