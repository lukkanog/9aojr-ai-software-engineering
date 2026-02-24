package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Document(collection = "exam_statistics")
public class ExamStatistics {

    @Id
    private String id;

    @Indexed(unique = true)
    private String examId;

    private Map<String, Double> percentualAcertoPorQuestao; // questionId -> %
    private Map<String, Integer> distribuicaoNotas; // faixa -> count
    private List<String> questoesComProblema;
    private Instant dataGeracao;

    public ExamStatistics() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public Map<String, Double> getPercentualAcertoPorQuestao() {
        return percentualAcertoPorQuestao;
    }

    public void setPercentualAcertoPorQuestao(Map<String, Double> percentualAcertoPorQuestao) {
        this.percentualAcertoPorQuestao = percentualAcertoPorQuestao;
    }

    public Map<String, Integer> getDistribuicaoNotas() {
        return distribuicaoNotas;
    }

    public void setDistribuicaoNotas(Map<String, Integer> distribuicaoNotas) {
        this.distribuicaoNotas = distribuicaoNotas;
    }

    public List<String> getQuestoesComProblema() {
        return questoesComProblema;
    }

    public void setQuestoesComProblema(List<String> questoesComProblema) {
        this.questoesComProblema = questoesComProblema;
    }

    public Instant getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Instant dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}
