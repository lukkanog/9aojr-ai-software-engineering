package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.dto.CorrectionResultResponse;
import com.projeto.examcorrection.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CorrectionServiceTest {

    @Mock
    private SubmissionRepository submissionRepository;
    @Mock
    private CorrectionResultRepository correctionResultRepository;
    @Mock
    private AnswerKeyRepository answerKeyRepository;
    @Mock
    private ExamService examService;
    @Mock
    private ExamReportRepository examReportRepository;
    @Mock
    private ExamStatisticsRepository examStatisticsRepository;

    @InjectMocks
    private CorrectionService correctionService;

    private Exam exam;
    private Submission submission;
    private AnswerKey answerKey;

    @BeforeEach
    void setUp() {
        Question q1 = new Question();
        q1.setId("q1");
        q1.setEnunciado("Qual a capital do Brasil?");
        q1.setTipo(QuestionType.OBJETIVA);
        q1.setAlternativas(List.of("A", "B", "C", "D"));
        q1.setPontuacao(2.0);
        q1.setOrdem(1);

        Question q2 = new Question();
        q2.setId("q2");
        q2.setEnunciado("Java é tipada?");
        q2.setTipo(QuestionType.VERDADEIRO_FALSO);
        q2.setAlternativas(List.of("V", "F"));
        q2.setPontuacao(1.0);
        q2.setOrdem(2);

        exam = new Exam();
        exam.setId("exam1");
        exam.setProfessorId("prof1");
        exam.setStatus(ExamStatus.PUBLICADA);
        exam.setQuestions(List.of(q1, q2));

        submission = new Submission();
        submission.setId("sub1");
        submission.setExamId("exam1");
        submission.setAlunoId("aluno1");
        submission.setRespostas(Map.of("q1", "B", "q2", "V"));
        submission.setCorrigida(false);
        submission.setDataEnvio(Instant.now());

        answerKey = new AnswerKey();
        answerKey.setId("ak1");
        answerKey.setExamId("exam1");
        answerKey.setRespostas(Map.of("q1", "B", "q2", "V"));
    }

    @Test
    void correct_shouldReturnPerfectScore_whenAllAnswersCorrect() {
        when(submissionRepository.findById("sub1")).thenReturn(Optional.of(submission));
        when(examService.findById("exam1")).thenReturn(exam);
        when(correctionResultRepository.existsBySubmissionId("sub1")).thenReturn(false);
        when(answerKeyRepository.findByExamId("exam1")).thenReturn(Optional.of(answerKey));
        when(correctionResultRepository.save(any())).thenAnswer(inv -> {
            CorrectionResult r = inv.getArgument(0);
            r.setId("cr1");
            return r;
        });
        when(submissionRepository.save(any())).thenReturn(submission);

        CorrectionResultResponse result = correctionService.correct("sub1", "prof1", Role.PROFESSOR);

        assertEquals(2, result.acertos());
        assertEquals(0, result.erros());
        assertEquals(3.0, result.notaFinal());
        assertEquals(2, result.detalhesPorQuestao().size());
        assertTrue(result.detalhesPorQuestao().stream().allMatch(QuestionDetail::isCorreta));

        verify(correctionResultRepository).save(any());
        verify(submissionRepository).save(any());
        verify(examReportRepository).deleteByExamId("exam1");
        verify(examStatisticsRepository).deleteByExamId("exam1");
    }

    @Test
    void correct_shouldReturnPartialScore_whenSomeAnswersWrong() {
        submission.setRespostas(Map.of("q1", "A", "q2", "V")); // q1 wrong

        when(submissionRepository.findById("sub1")).thenReturn(Optional.of(submission));
        when(examService.findById("exam1")).thenReturn(exam);
        when(correctionResultRepository.existsBySubmissionId("sub1")).thenReturn(false);
        when(answerKeyRepository.findByExamId("exam1")).thenReturn(Optional.of(answerKey));
        when(correctionResultRepository.save(any())).thenAnswer(inv -> {
            CorrectionResult r = inv.getArgument(0);
            r.setId("cr1");
            return r;
        });
        when(submissionRepository.save(any())).thenReturn(submission);

        CorrectionResultResponse result = correctionService.correct("sub1", "prof1", Role.PROFESSOR);

        assertEquals(1, result.acertos());
        assertEquals(1, result.erros());
        assertEquals(1.0, result.notaFinal());
    }

    @Test
    void correct_shouldThrow_whenAlreadyCorrected() {
        when(submissionRepository.findById("sub1")).thenReturn(Optional.of(submission));
        when(examService.findById("exam1")).thenReturn(exam);
        when(correctionResultRepository.existsBySubmissionId("sub1")).thenReturn(true);

        assertThrows(Exception.class, () -> correctionService.correct("sub1", "prof1", Role.PROFESSOR));
    }
}
