package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.AnswerKey;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface AnswerKeyRepository extends MongoRepository<AnswerKey, String> {
    Optional<AnswerKey> findByExamId(String examId);

    boolean existsByExamId(String examId);
}
