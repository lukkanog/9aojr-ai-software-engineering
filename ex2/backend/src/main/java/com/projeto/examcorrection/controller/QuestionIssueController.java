package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.dto.QuestionIssueRequest;
import com.projeto.examcorrection.dto.QuestionIssueResponse;
import com.projeto.examcorrection.service.QuestionIssueService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/questions/{questionId}/issues")
public class QuestionIssueController {

    private final QuestionIssueService questionIssueService;

    public QuestionIssueController(QuestionIssueService questionIssueService) {
        this.questionIssueService = questionIssueService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<List<QuestionIssueResponse>> findByQuestion(@PathVariable String questionId) {
        return ResponseEntity.ok(questionIssueService.findByQuestionId(questionId));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<QuestionIssueResponse> create(@PathVariable String questionId,
            @Valid @RequestBody QuestionIssueRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionIssueService.create(questionId, request, principal.getName()));
    }
}
