package com.projeto.examcorrection.domain;

import java.util.List;

public class Question {

    private String id;
    private String enunciado;
    private QuestionType tipo;
    private List<String> alternativas;
    private double pontuacao;
    private int ordem;

    public Question() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEnunciado() {
        return enunciado;
    }

    public void setEnunciado(String enunciado) {
        this.enunciado = enunciado;
    }

    public QuestionType getTipo() {
        return tipo;
    }

    public void setTipo(QuestionType tipo) {
        this.tipo = tipo;
    }

    public List<String> getAlternativas() {
        return alternativas;
    }

    public void setAlternativas(List<String> alternativas) {
        this.alternativas = alternativas;
    }

    public double getPontuacao() {
        return pontuacao;
    }

    public void setPontuacao(double pontuacao) {
        this.pontuacao = pontuacao;
    }

    public int getOrdem() {
        return ordem;
    }

    public void setOrdem(int ordem) {
        this.ordem = ordem;
    }
}
