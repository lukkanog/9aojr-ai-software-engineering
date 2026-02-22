package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.ExamReport;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ExamReportRepository extends MongoRepository<ExamReport, String> {
    Optional<ExamReport> findByExamId(String examId);

    void deleteByExamId(String examId);
}
