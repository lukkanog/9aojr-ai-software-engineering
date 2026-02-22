package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "question_issues")
public class QuestionIssue {

    @Id
    private String id;

    private String questionId;
    private String examId;
    private String tipoProblema;
    private Severidade severidade;
    private String descricao;
    private GeradoPor geradoPor;
    private Instant dataIdentificacao;

    public QuestionIssue() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getExamId() {
        return examId;
    }

    public void setExamId(String examId) {
        this.examId = examId;
    }

    public String getTipoProblema() {
        return tipoProblema;
    }

    public void setTipoProblema(String tipoProblema) {
        this.tipoProblema = tipoProblema;
    }

    public Severidade getSeveridade() {
        return severidade;
    }

    public void setSeveridade(Severidade severidade) {
        this.severidade = severidade;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public GeradoPor getGeradoPor() {
        return geradoPor;
    }

    public void setGeradoPor(GeradoPor geradoPor) {
        this.geradoPor = geradoPor;
    }

    public Instant getDataIdentificacao() {
        return dataIdentificacao;
    }

    public void setDataIdentificacao(Instant dataIdentificacao) {
        this.dataIdentificacao = dataIdentificacao;
    }
}
