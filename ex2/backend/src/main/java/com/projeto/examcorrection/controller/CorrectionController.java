package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.dto.CorrectionResultResponse;
import com.projeto.examcorrection.service.CorrectionService;
import com.projeto.examcorrection.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/submissions")
public class CorrectionController {

    private final CorrectionService correctionService;
    private final UserService userService;

    public CorrectionController(CorrectionService correctionService, UserService userService) {
        this.correctionService = correctionService;
        this.userService = userService;
    }

    @PostMapping("/{id}/correct")
    @PreAuthorize("hasRole('PROFESSOR') and @securityExpressions.isProfessorOfSubmission(authentication, #id)")
    public ResponseEntity<CorrectionResultResponse> correct(@PathVariable String id) {
        return ResponseEntity.ok(correctionService.correct(id));
    }

    @GetMapping("/{id}/correction-result")
    @PreAuthorize("(hasRole('ALUNO') and @securityExpressions.isSubmissionOwner(authentication, #id)) or (hasRole('PROFESSOR') and @securityExpressions.isProfessorOfSubmission(authentication, #id))")
    public ResponseEntity<CorrectionResultResponse> getResult(@PathVariable String id) {
        return ResponseEntity.ok(correctionService.getResult(id));
    }
}
