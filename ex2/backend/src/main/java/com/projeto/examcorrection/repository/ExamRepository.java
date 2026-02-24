package com.projeto.examcorrection.repository;

import com.projeto.examcorrection.domain.Exam;
import com.projeto.examcorrection.domain.ExamStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ExamRepository extends MongoRepository<Exam, String> {
    List<Exam> findByProfessorId(String professorId);

    List<Exam> findByStatus(ExamStatus status);
}
