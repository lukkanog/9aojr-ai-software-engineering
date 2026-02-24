package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.GeradoPor;
import com.projeto.examcorrection.domain.QuestionIssue;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface QuestionIssueRepository extends MongoRepository<QuestionIssue, String> {
    List<QuestionIssue> findByQuestionId(String questionId);

    Optional<QuestionIssue> findByQuestionIdAndTipoProblemaAndGeradoPor(String questionId, String tipoProblema,
            GeradoPor geradoPor);

    List<QuestionIssue> findByExamId(String examId);
}
