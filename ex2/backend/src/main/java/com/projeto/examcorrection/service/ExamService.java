package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.dto.*;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.AnswerKeyRepository;
import com.projeto.examcorrection.repository.ExamRepository;
import com.projeto.examcorrection.repository.SubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ExamService {

    private static final Logger log = LoggerFactory.getLogger(ExamService.class);

    private final ExamRepository examRepository;
    private final AnswerKeyRepository answerKeyRepository;
    private final SubmissionRepository submissionRepository;

    public ExamService(ExamRepository examRepository, AnswerKeyRepository answerKeyRepository,
            SubmissionRepository submissionRepository) {
        this.examRepository = examRepository;
        this.answerKeyRepository = answerKeyRepository;
        this.submissionRepository = submissionRepository;
    }

    public List<ExamResponse> findAll(String userId, Role role) {
        List<Exam> exams;
        if (role == Role.PROFESSOR) {
            exams = examRepository.findByProfessorId(userId);
        } else {
            exams = examRepository.findByStatus(ExamStatus.PUBLICADA);
            exams.addAll(examRepository.findByStatus(ExamStatus.ENCERRADA));
        }
        return exams.stream().map(this::toResponse).toList();
    }

    public ExamResponse findResponseById(String id, String userId, Role role) {
        Exam exam = findById(id);
        checkReadAccess(exam, userId, role);
        return toResponse(exam);
    }

    public Exam findById(String id) {
        return examRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EXAM_NOT_FOUND",
                        "Prova não encontrada com o ID informado."));
    }

    public ExamResponse create(ExamRequest request, String professorId) {
        Exam exam = new Exam();
        exam.setTitulo(request.titulo());
        exam.setDescricao(request.descricao());
        exam.setProfessorId(professorId);
        exam.setDataInicio(request.dataInicio());
        exam.setDataFim(request.dataFim());
        exam.setStatus(ExamStatus.RASCUNHO);
        exam.setDataCriacao(Instant.now());

        exam = examRepository.save(exam);
        log.info("Exam created: id={}, professor={}", exam.getId(), professorId);
        return toResponse(exam);
    }

    public ExamResponse update(String id, ExamRequest request, String professorId) {
        Exam exam = findById(id);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Apenas provas em RASCUNHO podem ser editadas.");

        exam.setTitulo(request.titulo());
        exam.setDescricao(request.descricao());
        exam.setDataInicio(request.dataInicio());
        exam.setDataFim(request.dataFim());

        exam = examRepository.save(exam);
        log.info("Exam updated: id={}", exam.getId());
        return toResponse(exam);
    }

    public void delete(String id, String professorId) {
        Exam exam = findById(id);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Apenas provas em RASCUNHO podem ser deletadas.");

        examRepository.delete(exam);
        log.info("Exam deleted: id={}", id);
    }

    public ExamResponse publish(String id, String professorId) {
        Exam exam = findById(id);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Apenas provas em RASCUNHO podem ser publicadas.");

        if (exam.getQuestions() == null || exam.getQuestions().isEmpty()) {
            throw new BusinessRuleException("EXAM_NO_QUESTIONS",
                    "A prova deve ter pelo menos 1 questão para ser publicada.");
        }

        var answerKey = answerKeyRepository.findByExamId(id)
                .orElseThrow(() -> new BusinessRuleException("ANSWER_KEY_REQUIRED",
                        "É necessário criar um gabarito antes de publicar a prova."));

        for (Question q : exam.getQuestions()) {
            if (!answerKey.getRespostas().containsKey(q.getId())) {
                throw new BusinessRuleException("ANSWER_KEY_INCOMPLETE",
                        "O gabarito não cobre todas as questões da prova. Faltando: " + q.getId());
            }
        }

        exam.setStatus(ExamStatus.PUBLICADA);
        exam = examRepository.save(exam);
        log.info("Exam published: id={}", exam.getId());
        return toResponse(exam);
    }

    public ExamResponse close(String id, String professorId) {
        Exam exam = findById(id);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.PUBLICADA, "Apenas provas PUBLICADAS podem ser encerradas.");

        exam.setStatus(ExamStatus.ENCERRADA);
        exam = examRepository.save(exam);
        log.info("Exam closed: id={}", exam.getId());
        return toResponse(exam);
    }

    // --- Questions as subdocument ---

    public List<Question> getQuestions(String examId, String userId, Role role) {
        Exam exam = findById(examId);
        checkReadAccess(exam, userId, role);
        return exam.getQuestions();
    }

    public Question addQuestion(String examId, QuestionRequest request, String professorId) {
        Exam exam = findById(examId);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser adicionadas em provas RASCUNHO.");

        if (request.tipo() == QuestionType.VERDADEIRO_FALSO && request.alternativas().size() != 2) {
            throw new BusinessRuleException("INVALID_ALTERNATIVES",
                    "Questões VERDADEIRO_FALSO devem ter exatamente 2 alternativas.");
        }

        Question question = new Question();
        question.setId(UUID.randomUUID().toString());
        question.setEnunciado(request.enunciado());
        question.setTipo(request.tipo());
        question.setAlternativas(request.alternativas());
        question.setPontuacao(request.pontuacao());
        question.setOrdem(request.ordem());

        exam.getQuestions().add(question);
        examRepository.save(exam);
        log.info("Question added to exam {}: questionId={}", examId, question.getId());
        return question;
    }

    public Question updateQuestion(String questionId, QuestionRequest request, String professorId) {
        Exam exam = findExamByQuestionId(questionId);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser editadas em provas RASCUNHO.");

        if (request.tipo() == QuestionType.VERDADEIRO_FALSO && request.alternativas().size() != 2) {
            throw new BusinessRuleException("INVALID_ALTERNATIVES",
                    "Questões VERDADEIRO_FALSO devem ter exatamente 2 alternativas.");
        }

        Question question = exam.getQuestions().stream()
                .filter(q -> q.getId().equals(questionId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("QUESTION_NOT_FOUND", "Questão não encontrada."));

        question.setEnunciado(request.enunciado());
        question.setTipo(request.tipo());
        question.setAlternativas(request.alternativas());
        question.setPontuacao(request.pontuacao());
        question.setOrdem(request.ordem());

        examRepository.save(exam);
        log.info("Question updated: id={}", questionId);
        return question;
    }

    public void deleteQuestion(String questionId, String professorId) {
        Exam exam = findExamByQuestionId(questionId);
        checkOwnership(exam, professorId);
        checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser removidas em provas RASCUNHO.");

        exam.getQuestions().removeIf(q -> q.getId().equals(questionId));
        examRepository.save(exam);
        log.info("Question deleted: id={}", questionId);
    }

    public Exam findExamByQuestionId(String questionId) {
        return examRepository.findAll().stream()
                .filter(e -> e.getQuestions().stream().anyMatch(q -> q.getId().equals(questionId)))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("QUESTION_NOT_FOUND",
                        "Questão não encontrada em nenhuma prova."));
    }

    private void checkOwnership(Exam exam, String professorId) {
        if (!exam.getProfessorId().equals(professorId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Você não tem permissão para acessar esta prova.",
                    HttpStatus.FORBIDDEN);
        }
    }

    private void checkReadAccess(Exam exam, String userId, Role role) {
        if (role == Role.PROFESSOR && !exam.getProfessorId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Você não tem permissão para acessar esta prova.",
                    HttpStatus.FORBIDDEN);
        }
        if (role == Role.ALUNO && exam.getStatus() == ExamStatus.RASCUNHO) {
            throw new ResourceNotFoundException("EXAM_NOT_FOUND", "Prova não encontrada.");
        }
    }

    private void checkStatus(Exam exam, ExamStatus expected, String message) {
        if (exam.getStatus() != expected) {
            throw new BusinessRuleException("INVALID_EXAM_STATUS", message);
        }
    }

    private ExamResponse toResponse(Exam exam) {
        return new ExamResponse(exam.getId(), exam.getTitulo(), exam.getDescricao(), exam.getProfessorId(),
                exam.getDataInicio(), exam.getDataFim(), exam.getStatus(), exam.getQuestions(), exam.getDataCriacao());
    }
}
