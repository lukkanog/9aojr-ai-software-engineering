package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.dto.AnswerKeyRequest;
import com.projeto.examcorrection.dto.AnswerKeyResponse;
import com.projeto.examcorrection.service.AnswerKeyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/exams/{examId}/answer-key")
public class AnswerKeyController {

    private final AnswerKeyService answerKeyService;

    public AnswerKeyController(AnswerKeyService answerKeyService) {
        this.answerKeyService = answerKeyService;
    }

    @GetMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AnswerKeyResponse> get(@PathVariable String examId, Principal principal) {
        return ResponseEntity.ok(answerKeyService.getByExamId(examId, principal.getName()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AnswerKeyResponse> create(@PathVariable String examId,
            @Valid @RequestBody AnswerKeyRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(answerKeyService.create(examId, request, principal.getName()));
    }

    @PutMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<AnswerKeyResponse> update(@PathVariable String examId,
            @Valid @RequestBody AnswerKeyRequest request,
            Principal principal) {
        return ResponseEntity.ok(answerKeyService.update(examId, request, principal.getName()));
    }
}
