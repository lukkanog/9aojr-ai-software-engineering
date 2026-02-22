package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.CorrectionResult;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface CorrectionResultRepository extends MongoRepository<CorrectionResult, String> {
    Optional<CorrectionResult> findBySubmissionId(String submissionId);

    boolean existsBySubmissionId(String submissionId);
}
