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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubmissionService {

    private static final Logger log = LoggerFactory.getLogger(SubmissionService.class);

    private final SubmissionRepository submissionRepository;
    private final ExamService examService;
    private final UserRepository userRepository;

    public SubmissionService(SubmissionRepository submissionRepository,
            ExamService examService,
            UserRepository userRepository) {
        this.submissionRepository = submissionRepository;
        this.examService = examService;
        this.userRepository = userRepository;
    }

    public List<SubmissionResponse> findByExamId(String examId, String userId, Role role) {
        Exam exam = examService.findById(examId);
        if (role == Role.PROFESSOR && !exam.getProfessorId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
        }

        List<Submission> subs;
        if (role == Role.ALUNO) {
            subs = submissionRepository.findByExamIdAndAlunoId(examId, userId)
                    .map(List::of).orElse(List.of());
        } else {
            subs = submissionRepository.findByExamId(examId);
        }

        // Resolve nomes em batch para evitar N+1 queries
        Set<String> alunoIds = subs.stream().map(Submission::getAlunoId).collect(Collectors.toSet());
        Map<String, String> nomesPorId = userRepository.findAllById(alunoIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u.getNome() != null ? u.getNome() : u.getEmail()));

        return subs.stream().map(s -> toResponse(s, nomesPorId.get(s.getAlunoId()))).toList();
    }

    public SubmissionResponse findResponseById(String id, String userId, Role role) {
        Submission sub = findById(id);
        checkAccess(sub, userId, role);
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

    private void checkAccess(Submission sub, String userId, Role role) {
        if (role == Role.ALUNO && !sub.getAlunoId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Você só pode acessar suas próprias submissões.",
                    HttpStatus.FORBIDDEN);
        }
        if (role == Role.PROFESSOR) {
            Exam exam = examService.findById(sub.getExamId());
            if (!exam.getProfessorId().equals(userId)) {
                throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
            }
        }
    }

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
