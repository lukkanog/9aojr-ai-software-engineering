package com.projeto.examcorrection.service;

import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.error.BusinessRuleException;
import com.projeto.examcorrection.error.ResourceNotFoundException;
import com.projeto.examcorrection.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportService.class);

    private final ExamService examService;
    private final SubmissionRepository submissionRepository;
    private final CorrectionResultRepository correctionResultRepository;
    private final ExamReportRepository examReportRepository;
    private final ExamStatisticsRepository examStatisticsRepository;
    private final QuestionIssueRepository questionIssueRepository;

    @Value("${app.correction.min-submissions-for-issue}")
    private int minSubmissionsForIssue;

    @Value("${app.correction.threshold-low-accuracy}")
    private double thresholdLowAccuracy;

    @Value("${app.correction.threshold-high-accuracy}")
    private double thresholdHighAccuracy;

    @Value("${app.correction.threshold-high-blank}")
    private double thresholdHighBlank;

    public ReportService(ExamService examService, SubmissionRepository submissionRepository,
            CorrectionResultRepository correctionResultRepository,
            ExamReportRepository examReportRepository,
            ExamStatisticsRepository examStatisticsRepository,
            QuestionIssueRepository questionIssueRepository) {
        this.examService = examService;
        this.submissionRepository = submissionRepository;
        this.correctionResultRepository = correctionResultRepository;
        this.examReportRepository = examReportRepository;
        this.examStatisticsRepository = examStatisticsRepository;
        this.questionIssueRepository = questionIssueRepository;
    }

    public ExamReport getReport(String examId, String userId) {
        Exam exam = examService.findById(examId);
        if (!exam.getProfessorId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
        }

        Optional<ExamReport> cached = examReportRepository.findByExamId(examId);
        if (cached.isPresent())
            return cached.get();

        List<Submission> corrected = submissionRepository.findByExamIdAndCorrigida(examId, true);
        if (corrected.isEmpty()) {
            throw new BusinessRuleException("NO_CORRECTED_SUBMISSIONS",
                    "Não há submissões corrigidas para gerar relatório.");
        }

        List<Double> notas = corrected.stream().map(Submission::getNota).toList();
        double media = notas.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double maior = notas.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double menor = notas.stream().mapToDouble(Double::doubleValue).min().orElse(0);

        ExamReport report = new ExamReport();
        report.setExamId(examId);
        report.setMediaNotas(Math.round(media * 100.0) / 100.0);
        report.setMaiorNota(maior);
        report.setMenorNota(menor);
        report.setTotalSubmissoes(corrected.size());
        report.setDataGeracao(Instant.now());

        report = examReportRepository.save(report);
        log.info("Report generated for exam {}", examId);
        return report;
    }

    public ExamStatistics getStatistics(String examId, String userId) {
        Exam exam = examService.findById(examId);
        if (!exam.getProfessorId().equals(userId)) {
            throw new BusinessRuleException("ACCESS_DENIED", "Acesso negado.", HttpStatus.FORBIDDEN);
        }

        Optional<ExamStatistics> cached = examStatisticsRepository.findByExamId(examId);
        if (cached.isPresent())
            return cached.get();

        List<Submission> corrected = submissionRepository.findByExamIdAndCorrigida(examId, true);
        if (corrected.isEmpty()) {
            throw new BusinessRuleException("NO_CORRECTED_SUBMISSIONS",
                    "Não há submissões corrigidas para gerar estatísticas.");
        }

        int total = corrected.size();
        List<Question> questions = exam.getQuestions();

        // Percentual de acerto por questão
        Map<String, Double> percentualAcerto = new LinkedHashMap<>();
        for (Question q : questions) {
            long acertos = corrected.stream()
                    .filter(s -> {
                        CorrectionResult cr = correctionResultRepository.findBySubmissionId(s.getId()).orElse(null);
                        if (cr == null)
                            return false;
                        return cr.getDetalhesPorQuestao().stream()
                                .filter(d -> d.getQuestionId().equals(q.getId()))
                                .findFirst()
                                .map(QuestionDetail::isCorreta)
                                .orElse(false);
                    }).count();
            double pct = Math.round((acertos * 100.0 / total) * 100.0) / 100.0;
            percentualAcerto.put(q.getId(), pct);
        }

        // Distribuição de notas (faixas de 10%)
        double maxNota = questions.stream().mapToDouble(Question::getPontuacao).sum();
        Map<String, Integer> distribuicao = new LinkedHashMap<>();
        for (int i = 0; i < 10; i++) {
            String faixa = (i * 10) + "-" + ((i + 1) * 10) + "%";
            distribuicao.put(faixa, 0);
        }
        for (Submission s : corrected) {
            double pct = maxNota > 0 ? (s.getNota() / maxNota) * 100.0 : 0;
            int bucket = Math.min((int) (pct / 10), 9);
            String faixa = (bucket * 10) + "-" + ((bucket + 1) * 10) + "%";
            distribuicao.merge(faixa, 1, Integer::sum);
        }

        // Questões com problema
        List<String> questoesComProblema = questionIssueRepository.findByExamId(examId).stream()
                .map(QuestionIssue::getQuestionId)
                .distinct()
                .toList();

        ExamStatistics stats = new ExamStatistics();
        stats.setExamId(examId);
        stats.setPercentualAcertoPorQuestao(percentualAcerto);
        stats.setDistribuicaoNotas(distribuicao);
        stats.setQuestoesComProblema(questoesComProblema);
        stats.setDataGeracao(Instant.now());

        stats = examStatisticsRepository.save(stats);
        log.info("Statistics generated for exam {}", examId);

        // Auto-detect question issues
        if (total >= minSubmissionsForIssue) {
            generateAutoIssues(exam, percentualAcerto, corrected);
        }

        return stats;
    }

    private void generateAutoIssues(Exam exam, Map<String, Double> percentualAcerto, List<Submission> corrected) {
        int total = corrected.size();

        for (Question q : exam.getQuestions()) {
            double pctAcerto = percentualAcerto.getOrDefault(q.getId(), 0.0);

            // Branco rate
            long blanks = corrected.stream()
                    .filter(s -> s.getRespostas() == null || !s.getRespostas().containsKey(q.getId())
                            || s.getRespostas().get(q.getId()) == null)
                    .count();
            double pctBranco = (blanks * 100.0 / total);

            checkAndCreateIssue(q, exam.getId(), "MUITO_BAIXO_ACERTO", pctAcerto < thresholdLowAccuracy,
                    Severidade.ALTA,
                    "Taxa de acerto muito baixa: " + String.format("%.1f%%", pctAcerto));

            checkAndCreateIssue(q, exam.getId(), "MUITO_ALTO_ACERTO", pctAcerto > thresholdHighAccuracy,
                    Severidade.BAIXA,
                    "Taxa de acerto muito alta: " + String.format("%.1f%%", pctAcerto));

            checkAndCreateIssue(q, exam.getId(), "ALTO_INDICE_BRANCO", pctBranco > thresholdHighBlank, Severidade.MEDIA,
                    "Alto índice de questões em branco: " + String.format("%.1f%%", pctBranco));
        }
    }

    private void checkAndCreateIssue(Question q, String examId, String tipo, boolean condition, Severidade severidade,
            String descricao) {
        Optional<QuestionIssue> existing = questionIssueRepository
                .findByQuestionIdAndTipoProblemaAndGeradoPor(q.getId(), tipo, GeradoPor.SISTEMA);

        if (condition) {
            if (existing.isPresent()) {
                QuestionIssue issue = existing.get();
                issue.setDescricao(descricao);
                issue.setSeveridade(severidade);
                issue.setDataIdentificacao(Instant.now());
                questionIssueRepository.save(issue);
            } else {
                QuestionIssue issue = new QuestionIssue();
                issue.setQuestionId(q.getId());
                issue.setExamId(examId);
                issue.setTipoProblema(tipo);
                issue.setSeveridade(severidade);
                issue.setDescricao(descricao);
                issue.setGeradoPor(GeradoPor.SISTEMA);
                issue.setDataIdentificacao(Instant.now());
                questionIssueRepository.save(issue);
            }
        }
    }
}
