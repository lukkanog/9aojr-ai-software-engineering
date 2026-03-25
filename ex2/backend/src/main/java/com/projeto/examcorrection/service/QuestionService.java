package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.Exam;
import com.projeto.examcorrection.domain.ExamStatus;
import com.projeto.examcorrection.domain.Question;
import com.projeto.examcorrection.domain.QuestionType;
import com.projeto.examcorrection.domain.Role;
import com.projeto.examcorrection.dto.QuestionRequest;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.ExamRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionService.class);

    private final ExamService examService;
    private final ExamRepository examRepository;

    public QuestionService(ExamService examService, ExamRepository examRepository) {
        this.examService = examService;
        this.examRepository = examRepository;
    }

    public List<Question> getQuestions(String examId, String userId, Role role) {
        Exam exam = examService.findById(examId);
        examService.checkReadAccess(exam, userId, role);
        return exam.getQuestions();
    }

    public Question addQuestion(String examId, QuestionRequest request, String professorId) {
        Exam exam = examService.findById(examId);
        examService.checkOwnership(exam, professorId);
        examService.checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser adicionadas em provas RASCUNHO.");

        validateQuestionRequest(request);

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
        examService.checkOwnership(exam, professorId);
        examService.checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser editadas em provas RASCUNHO.");

        validateQuestionRequest(request);

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
        examService.checkOwnership(exam, professorId);
        examService.checkStatus(exam, ExamStatus.RASCUNHO, "Questões só podem ser removidas em provas RASCUNHO.");

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

    private void validateQuestionRequest(QuestionRequest request) {
        if (request.tipo() == QuestionType.VERDADEIRO_FALSO && request.alternativas().size() != 2) {
            throw new BusinessRuleException("INVALID_ALTERNATIVES",
                    "Questões VERDADEIRO_FALSO devem ter exatamente 2 alternativas.");
        }
    }
}
