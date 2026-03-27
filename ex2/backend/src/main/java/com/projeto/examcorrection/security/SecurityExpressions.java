package com.projeto.examcorrection.security;

import com.projeto.examcorrection.repository.ExamRepository;
import com.projeto.examcorrection.repository.SubmissionRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("securityExpressions")
public class SecurityExpressions {

    private final ExamRepository examRepository;
    private final SubmissionRepository submissionRepository;

    public SecurityExpressions(ExamRepository examRepository, SubmissionRepository submissionRepository) {
        this.examRepository = examRepository;
        this.submissionRepository = submissionRepository;
    }

    public boolean isExamOwner(Authentication authentication, String examId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        String userId = authentication.getPrincipal().toString();
        return examRepository.findById(examId)
                .map(exam -> exam.isOwner(userId))
                .orElse(false);
    }

    public boolean isSubmissionOwner(Authentication authentication, String submissionId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        String userId = authentication.getPrincipal().toString();
        return submissionRepository.findById(submissionId)
                .map(submission -> submission.isOwner(userId))
                .orElse(false);
    }

    public boolean isProfessorOfSubmission(Authentication authentication, String submissionId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        String userId = authentication.getPrincipal().toString();
        return submissionRepository.findById(submissionId)
                .flatMap(sub -> examRepository.findById(sub.getExamId()))
                .map(exam -> exam.isOwner(userId))
                .orElse(false);
    }

    public boolean isExamOwnerByQuestion(Authentication authentication, String questionId) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return false;
        }
        String userId = authentication.getPrincipal().toString();
        return examRepository.findAll().stream()
                .filter(e -> e.getQuestions() != null && e.getQuestions().stream().anyMatch(q -> q.getId().equals(questionId)))
                .findFirst()
                .map(exam -> exam.isOwner(userId))
                .orElse(false);
    }
}
