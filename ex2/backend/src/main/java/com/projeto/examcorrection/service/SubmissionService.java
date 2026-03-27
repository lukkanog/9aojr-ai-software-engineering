package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.dto.SubmissionRequest;
import com.projeto.examcorrection.dto.SubmissionResponse;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ConflictException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.SubmissionRepository;
import com.projeto.examcorrection.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final ExamService examService;
    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;

    public SubmissionService(SubmissionRepository submissionRepository,
            ExamService examService,
            UserRepository userRepository,
            MongoTemplate mongoTemplate) {
        this.submissionRepository = submissionRepository;
        this.examService = examService;
        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
    }

    public List<SubmissionResponse> findByExamId(String examId, String userId, String roleAuthority) {
        Criteria criteria = Criteria.where("examId").is(examId);
        if ("ROLE_ALUNO".equals(roleAuthority)) {
            criteria.and("alunoId").is(userId);
        }

        Aggregation agg = Aggregation.newAggregation(
                Aggregation.match(criteria),
                Aggregation.lookup("users", "alunoId", "_id", "studentInfo"),
                Aggregation.unwind("studentInfo", true),
                Aggregation.project("id", "examId", "alunoId", "respostas", "nota", "corrigida", "dataEnvio")
                        .andExpression("ifNull($studentInfo.nome, ifNull($studentInfo.email, 'UNKNOWN'))")
                        .as("alunoNome")
        );

        return mongoTemplate.aggregate(agg, "submissions", SubmissionResponse.class).getMappedResults();
    }

    public SubmissionResponse findResponseById(String id) {
        Submission sub = findById(id);
        String alunoNome = resolveNome(sub.getAlunoId());
        return toResponse(sub, alunoNome);
    }

    public Submission findById(String id) {
        return submissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SUBMISSION_NOT_FOUND", "Submissão não encontrada."));
    }

    public SubmissionResponse create(String examId, SubmissionRequest request, String alunoId) {
        Exam exam = examService.findById(examId);

        if (exam.getStatus() != ExamStatus.PUBLICADA) {
            throw new BusinessRuleException("EXAM_NOT_PUBLISHED", "A prova não está publicada para submissões.");
        }

        Instant now = Instant.now();
        if (exam.getDataInicio() != null && now.isBefore(exam.getDataInicio())) {
            throw new BusinessRuleException("EXAM_NOT_STARTED", "A prova ainda não está disponível para submissões.");
        }
        if (exam.getDataFim() != null && now.isAfter(exam.getDataFim())) {
            throw new BusinessRuleException("EXAM_EXPIRED", "O prazo para submissões desta prova já encerrou.");
        }

        if (submissionRepository.existsByExamIdAndAlunoId(examId, alunoId)) {
            throw new ConflictException("SUBMISSION_ALREADY_EXISTS", "Você já enviou uma submissão para esta prova.");
        }

        // Validate alternatives
        Set<String> questionIds = exam.getQuestions().stream().map(Question::getId).collect(Collectors.toSet());
        for (var entry : request.respostas().entrySet()) {
            if (!questionIds.contains(entry.getKey())) {
                throw new BusinessRuleException("INVALID_QUESTION_ID", "Questão inválida: " + entry.getKey(),
                        HttpStatus.BAD_REQUEST);
            }
            Question q = exam.getQuestions().stream().filter(x -> x.getId().equals(entry.getKey())).findFirst()
                    .orElse(null);
            if (q != null && !q.getAlternativas().contains(entry.getValue())) {
                throw new BusinessRuleException("INVALID_ALTERNATIVE",
                        "Alternativa inválida para questão " + entry.getKey() + ": " + entry.getValue(),
                        HttpStatus.BAD_REQUEST);
            }
        }

        Submission sub = new Submission();
        sub.setExamId(examId);
        sub.setAlunoId(alunoId);
        sub.setRespostas(request.respostas());
        sub.setCorrigida(false);
        sub.setDataEnvio(now);

        sub = submissionRepository.save(sub);
        log.info("Submission created: id={}, exam={}, aluno={}", sub.getId(), examId, alunoId);
        return toResponse(sub, resolveNome(alunoId));
    }

    // checkAccess removed, replaced by Method Security

    /**
     * Resolve o nome do aluno pelo ID, com fallback para o email ou o próprio ID.
     */
    private String resolveNome(String alunoId) {
        return userRepository.findById(alunoId)
                .map(u -> u.getNome() != null && !u.getNome().isBlank() ? u.getNome() : u.getEmail())
                .orElse(alunoId);
    }

    private SubmissionResponse toResponse(Submission sub, String alunoNome) {
        return new SubmissionResponse(
                sub.getId(),
                sub.getExamId(),
                sub.getAlunoId(),
                alunoNome,
                sub.getRespostas(),
                sub.getNota(),
                sub.isCorrigida(),
                sub.getDataEnvio());
    }
}
