package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "exam_reports")
public class ExamReport {

    @Id
    private String id;

    @Indexed(unique = true)
    private String examId;

    private double mediaNotas;
    private double maiorNota;
    private double menorNota;
    private int totalSubmissoes;
    private Instant dataGeracao;

    public ExamReport() {
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

    public double getMediaNotas() {
        return mediaNotas;
    }

    public void setMediaNotas(double mediaNotas) {
        this.mediaNotas = mediaNotas;
    }

    public double getMaiorNota() {
        return maiorNota;
    }

    public void setMaiorNota(double maiorNota) {
        this.maiorNota = maiorNota;
    }

    public double getMenorNota() {
        return menorNota;
    }

    public void setMenorNota(double menorNota) {
        this.menorNota = menorNota;
    }

    public int getTotalSubmissoes() {
        return totalSubmissoes;
    }

    public void setTotalSubmissoes(int totalSubmissoes) {
        this.totalSubmissoes = totalSubmissoes;
    }

    public Instant getDataGeracao() {
        return dataGeracao;
    }

    public void setDataGeracao(Instant dataGeracao) {
        this.dataGeracao = dataGeracao;
    }
}
