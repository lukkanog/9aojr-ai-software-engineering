package com.projeto.examcorrection.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.projeto.examcorrection.domain.*;
import com.projeto.examcorrection.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.time.Instant;
import java.util.*;

@Configuration
public class DataSeeder {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    /** Chave única que identifica esta versão do seed. */
    private static final String SEED_KEY = "mock-v1";

    @Bean
    public CommandLineRunner seedData(
            SeedMetadataRepository seedMetadataRepository,
            UserRepository userRepository,
            ExamRepository examRepository,
            AnswerKeyRepository answerKeyRepository,
            SubmissionRepository submissionRepository,
            ObjectMapper objectMapper
    ) {
        return args -> {
            if (seedMetadataRepository.existsByChave(SEED_KEY)) {
                log.info("Seed '{}' já foi aplicado anteriormente. Ignorando.", SEED_KEY);
                return;
            }

            log.info("Aplicando seed '{}'...", SEED_KEY);

            ClassPathResource resource = new ClassPathResource("mock.json");
            try (InputStream is = resource.getInputStream()) {
                JsonNode root = objectMapper.readTree(is);

                // ── Usuários ──────────────────────────────────────────────
                List<User> users = new ArrayList<>();
                for (JsonNode node : root.get("users")) {
                    User u = new User();
                    u.setId(node.get("id").asText());
                    u.setNome(node.get("nome").asText());
                    u.setEmail(node.get("email").asText());
                    u.setSenhaHash(node.get("senhaHash").asText());
                    u.setRole(Role.valueOf(node.get("role").asText()));
                    u.setAtivo(node.get("ativo").asBoolean());
                    u.setDataCriacao(Instant.parse(node.get("dataCriacao").asText()));
                    users.add(u);
                }
                userRepository.saveAll(users);
                log.info("{} usuário(s) carregado(s).", users.size());

                // ── Provas ────────────────────────────────────────────────
                List<Exam> exams = new ArrayList<>();
                for (JsonNode node : root.get("exams")) {
                    Exam e = new Exam();
                    e.setId(node.get("id").asText());
                    e.setTitulo(node.get("titulo").asText());
                    e.setDescricao(node.get("descricao").asText());
                    e.setProfessorId(node.get("professorId").asText());
                    e.setDataInicio(Instant.parse(node.get("dataInicio").asText()));
                    e.setDataFim(Instant.parse(node.get("dataFim").asText()));
                    e.setStatus(ExamStatus.valueOf(node.get("status").asText()));
                    e.setDataCriacao(Instant.parse(node.get("dataCriacao").asText()));

                    List<Question> questions = new ArrayList<>();
                    for (JsonNode qNode : node.get("questions")) {
                        Question q = new Question();
                        q.setId(qNode.get("id").asText());
                        q.setEnunciado(qNode.get("enunciado").asText());
                        q.setTipo(QuestionType.valueOf(qNode.get("tipo").asText()));
                        q.setPontuacao(qNode.get("pontuacao").asDouble());
                        q.setOrdem(qNode.get("ordem").asInt());
                        List<String> alternativas = new ArrayList<>();
                        qNode.get("alternativas").forEach(a -> alternativas.add(a.asText()));
                        q.setAlternativas(alternativas);
                        questions.add(q);
                    }
                    e.setQuestions(questions);
                    exams.add(e);
                }
                examRepository.saveAll(exams);
                log.info("{} prova(s) carregada(s).", exams.size());

                // ── Gabaritos ─────────────────────────────────────────────
                List<AnswerKey> answerKeys = new ArrayList<>();
                for (JsonNode node : root.get("answerKeys")) {
                    AnswerKey ak = new AnswerKey();
                    ak.setId(node.get("id").asText());
                    ak.setExamId(node.get("examId").asText());
                    ak.setDataCriacao(Instant.parse(node.get("dataCriacao").asText()));
                    ak.setDataAtualizacao(Instant.parse(node.get("dataAtualizacao").asText()));
                    Map<String, String> respostas = new LinkedHashMap<>();
                    node.get("respostas").fields().forEachRemaining(e -> respostas.put(e.getKey(), e.getValue().asText()));
                    ak.setRespostas(respostas);
                    answerKeys.add(ak);
                }
                answerKeyRepository.saveAll(answerKeys);
                log.info("{} gabarito(s) carregado(s).", answerKeys.size());

                // ── Submissões ────────────────────────────────────────────
                List<Submission> submissions = new ArrayList<>();
                for (JsonNode node : root.get("submissions")) {
                    Submission s = new Submission();
                    s.setId(node.get("id").asText());
                    s.setExamId(node.get("examId").asText());
                    s.setAlunoId(node.get("alunoId").asText());
                    s.setNota(node.get("nota").asDouble());
                    s.setCorrigida(node.get("corrigida").asBoolean());
                    s.setDataEnvio(Instant.parse(node.get("dataEnvio").asText()));
                    Map<String, String> respostas = new LinkedHashMap<>();
                    node.get("respostas").fields().forEachRemaining(e -> respostas.put(e.getKey(), e.getValue().asText()));
                    s.setRespostas(respostas);
                    submissions.add(s);
                }
                submissionRepository.saveAll(submissions);
                log.info("{} submissão(ões) carregada(s).", submissions.size());

                // ── Marca o seed como aplicado ────────────────────────────
                seedMetadataRepository.save(new SeedMetadata(SEED_KEY, Instant.now()));
                log.info("Seed '{}' concluído e registrado com sucesso.", SEED_KEY);
            }
        };
    }
}
