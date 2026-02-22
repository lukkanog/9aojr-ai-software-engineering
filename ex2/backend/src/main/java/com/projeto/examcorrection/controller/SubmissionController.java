package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.dto.SubmissionRequest;
import com.projeto.examcorrection.dto.SubmissionResponse;
import com.projeto.examcorrection.service.SubmissionService;
import com.projeto.examcorrection.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
public class SubmissionController {

    private final SubmissionService submissionService;
    private final UserService userService;

    public SubmissionController(SubmissionService submissionService, UserService userService) {
        this.submissionService = submissionService;
        this.userService = userService;
    }

    @GetMapping("/exams/{examId}/submissions")
    public ResponseEntity<List<SubmissionResponse>> findByExam(@PathVariable String examId, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(submissionService.findByExamId(examId, user.getId(), user.getRole()));
    }

    @GetMapping("/submissions/{id}")
    public ResponseEntity<SubmissionResponse> findById(@PathVariable String id, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(submissionService.findResponseById(id, user.getId(), user.getRole()));
    }

    @PostMapping("/exams/{examId}/submissions")
    @PreAuthorize("hasRole('ALUNO')")
    public ResponseEntity<SubmissionResponse> create(@PathVariable String examId,
            @Valid @RequestBody SubmissionRequest request,
            Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(submissionService.create(examId, request, principal.getName()));
    }
}
