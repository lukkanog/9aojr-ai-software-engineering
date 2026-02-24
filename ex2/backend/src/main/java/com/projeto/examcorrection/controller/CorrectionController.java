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
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<CorrectionResultResponse> correct(@PathVariable String id, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(correctionService.correct(id, user.getId(), user.getRole()));
    }

    @GetMapping("/{id}/correction-result")
    public ResponseEntity<CorrectionResultResponse> getResult(@PathVariable String id, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(correctionService.getResult(id, user.getId(), user.getRole()));
    }
}
