package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.domain.Question;
import com.projeto.examcorrection.domain.Role;
import com.projeto.examcorrection.dto.QuestionRequest;
import com.projeto.examcorrection.service.ExamService;
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

    private final ExamService examService;
    private final UserService userService;

    public QuestionController(ExamService examService, UserService userService) {
        this.examService = examService;
        this.userService = userService;
    }

    @GetMapping("/exams/{examId}/questions")
    public ResponseEntity<List<Question>> getQuestions(@PathVariable String examId, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(examService.getQuestions(examId, user.getId(), user.getRole()));
    }

    @PostMapping("/exams/{examId}/questions")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Question> addQuestion(@PathVariable String examId,
            @Valid @RequestBody QuestionRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(examService.addQuestion(examId, request, principal.getName()));
    }

    @PutMapping("/questions/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Question> updateQuestion(@PathVariable String id,
            @Valid @RequestBody QuestionRequest request,
            Principal principal) {
        return ResponseEntity.ok(examService.updateQuestion(id, request, principal.getName()));
    }

    @DeleteMapping("/questions/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable String id, Principal principal) {
        examService.deleteQuestion(id, principal.getName());
        return ResponseEntity.noContent().build();
    }
}
