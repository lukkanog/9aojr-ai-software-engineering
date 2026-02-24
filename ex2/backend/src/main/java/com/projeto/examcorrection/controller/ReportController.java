package com.projeto.examcorrection.controller;

import com.projeto.examcorrection.domain.ExamReport;
import com.projeto.examcorrection.domain.ExamStatistics;
import com.projeto.examcorrection.service.ReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/exams/{examId}")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/report")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamReport> getReport(@PathVariable String examId, Principal principal) {
        return ResponseEntity.ok(reportService.getReport(examId, principal.getName()));
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasRole('PROFESSOR')")
    public ResponseEntity<ExamStatistics> getStatistics(@PathVariable String examId, Principal principal) {
        return ResponseEntity.ok(reportService.getStatistics(examId, principal.getName()));
    }
}
