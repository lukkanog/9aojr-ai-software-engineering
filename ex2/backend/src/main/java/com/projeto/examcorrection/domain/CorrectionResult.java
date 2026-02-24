package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "correction_results")
public class CorrectionResult {

    @Id
    private String id;

    @Indexed(unique = true)
    private String submissionId;

    private int acertos;
    private int erros;
    private double notaFinal;
    private List<QuestionDetail> detalhesPorQuestao;

    public CorrectionResult() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public int getAcertos() {
        return acertos;
    }

    public void setAcertos(int acertos) {
        this.acertos = acertos;
    }

    public int getErros() {
        return erros;
    }

    public void setErros(int erros) {
        this.erros = erros;
    }

    public double getNotaFinal() {
        return notaFinal;
    }

    public void setNotaFinal(double notaFinal) {
        this.notaFinal = notaFinal;
    }

    public List<QuestionDetail> getDetalhesPorQuestao() {
        return detalhesPorQuestao;
    }

    public void setDetalhesPorQuestao(List<QuestionDetail> detalhesPorQuestao) {
        this.detalhesPorQuestao = detalhesPorQuestao;
    }
}
