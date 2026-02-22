package com.projeto.examcorrection.domain;

public class QuestionDetail {

    private String questionId;
    private boolean correta;
    private String respostaAluno;
    private String respostaEsperada;
    private double pontuacaoObtida;

    public QuestionDetail() {
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public boolean isCorreta() {
        return correta;
    }

    public void setCorreta(boolean correta) {
        this.correta = correta;
    }

    public String getRespostaAluno() {
        return respostaAluno;
    }

    public void setRespostaAluno(String respostaAluno) {
        this.respostaAluno = respostaAluno;
    }

    public String getRespostaEsperada() {
        return respostaEsperada;
    }

    public void setRespostaEsperada(String respostaEsperada) {
        this.respostaEsperada = respostaEsperada;
    }

    public double getPontuacaoObtida() {
        return pontuacaoObtida;
    }

    public void setPontuacaoObtida(double pontuacaoObtida) {
        this.pontuacaoObtida = pontuacaoObtida;
    }
}
