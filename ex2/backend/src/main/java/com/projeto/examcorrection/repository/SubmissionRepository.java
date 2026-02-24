package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.Submission;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface SubmissionRepository extends MongoRepository<Submission, String> {
    List<Submission> findByExamId(String examId);

    Optional<Submission> findByExamIdAndAlunoId(String examId, String alunoId);

    boolean existsByExamId(String examId);

    boolean existsByExamIdAndAlunoId(String examId, String alunoId);

    List<Submission> findByExamIdAndCorrigida(String examId, boolean corrigida);
}
