package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.domain.Role;
import com.projeto.examcorrection.dto.ExamRequest;
import com.projeto.examcorrection.dto.ExamResponse;
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
@RequestMapping("/exams")
public class ExamController {

    private final ExamService examService;
    private final UserService userService;

    public ExamController(ExamService examService, UserService userService) {
        this.examService = examService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<List<ExamResponse>> findAll(Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(examService.findAll(user.getId(), user.getRole()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExamResponse> findById(@PathVariable String id, Principal principal) {
        var user = userService.findById(principal.getName());
        return ResponseEntity.ok(examService.findResponseById(id, user.getId(), user.getRole()));
    }

    @PostMapping
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamResponse> create(@Valid @RequestBody ExamRequest request, Principal principal) {
        return ResponseEntity.status(HttpStatus.CREATED).body(examService.create(request, principal.getName()));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamResponse> update(@PathVariable String id, @Valid @RequestBody ExamRequest request,
            Principal principal) {
        return ResponseEntity.ok(examService.update(id, request, principal.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<Void> delete(@PathVariable String id, Principal principal) {
        examService.delete(id, principal.getName());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamResponse> publish(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(examService.publish(id, principal.getName()));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamResponse> close(@PathVariable String id, Principal principal) {
        return ResponseEntity.ok(examService.close(id, principal.getName()));
    }
}
