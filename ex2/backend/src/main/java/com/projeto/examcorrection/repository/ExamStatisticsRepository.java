package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.ExamStatistics;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ExamStatisticsRepository extends MongoRepository<ExamStatistics, String> {
    Optional<ExamStatistics> findByExamId(String examId);

    void deleteByExamId(String examId);
}
