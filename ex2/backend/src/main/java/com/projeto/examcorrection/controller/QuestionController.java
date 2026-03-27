package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.domain.Question;
import com.projeto.examcorrection.domain.Role;
import com.projeto.examcorrection.dto.QuestionRequest;
import com.projeto.examcorrection.service.QuestionService;
import com.projeto.examcorrection.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class QuestionController {
    private final QuestionService questionService;
    private final UserService userService;

    public QuestionController(QuestionService questionService, UserService userService) {
        this.questionService = questionService;
        this.userService = userService;
    }

    @GetMapping("/exams/{examId}/questions")
    @PreAuthorize("hasRole('ALUNO') or (hasRole('PROFESSOR') and @securityExpressions.isExamOwner(authentication, #examId))")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable String examId) {
        return ResponseEntity.ok(questionService.getQuestions(examId));
    }

    @PostMapping("/exams/{examId}/questions")
    @PreAuthorize("hasRole('PROFESSOR') and @securityExpressions.isExamOwner(authentication, #examId)")
    public ResponseEntity<Question> addQuestion(@PathVariable String examId,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.addQuestion(examId, request));
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('PROFESSOR') and @securityExpressions.isExamOwnerByQuestion(authentication, #id)")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id,
            @Valid @RequestBody QuestionRequest request) {
        return ResponseEntity.ok(questionService.updateQuestion(id, request));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('PROFESSOR') and @securityExpressions.isExamOwnerByQuestion(authentication, #id)")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
