package com.projeto.examcorrection.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document(collection = "submissions")
@CompoundIndex(name = "exam_aluno_unique", def = "{'examId': 1, 'alunoId': 1}", unique = true)
public class Submission {

    @Id
    private String id;

    private String examId;
    private String alunoId;
    private Map<String, String> respostas; // questionId -> alternativaSelecionada
    private Double nota;
    private boolean corrigida;
    private Instant dataEnvio;

    public Submission() {
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

    public String getAlunoId() {
        return alunoId;
    }

    public void setAlunoId(String alunoId) {
        this.alunoId = alunoId;
    }

    public Map<String, String> getRespostas() {
        return respostas;
    }

    public void setRespostas(Map<String, String> respostas) {
        this.respostas = respostas;
    }

    public Double getNota() {
        return nota;
    }

    public void setNota(Double nota) {
        this.nota = nota;
    }

    public boolean isCorrigida() {
        return corrigida;
    }

    public void setCorrigida(boolean corrigida) {
        this.corrigida = corrigida;
    }

    public Instant getDataEnvio() {
        return dataEnvio;
    }

    public void setDataEnvio(Instant dataEnvio) {
        this.dataEnvio = dataEnvio;
    }
}
