# Entity Relationship Diagram — ExamCorrection

> **Database**: MongoDB (document-oriented)
> Relationships between collections are maintained via reference IDs (not foreign keys).

```mermaid
erDiagram

    USER {
        string id PK
        string nome
        string email UK
        string senhaHash
        enum role "PROFESSOR | ALUNO"
        boolean ativo
        instant dataCriacao
    }

    EXAM {
        string id PK
        string titulo
        string descricao
        string professorId FK
        instant dataInicio
        instant dataFim
        enum status "RASCUNHO | PUBLICADA | ENCERRADA"
        instant dataCriacao
    }

    QUESTION {
        string id
        string enunciado
        enum tipo "MULTIPLA_ESCOLHA | ..."
        list alternativas
        double pontuacao
        int ordem
    }

    ANSWER_KEY {
        string id PK
        string examId FK "unique"
        map respostas "questionId -> alternativaCorreta"
        instant dataCriacao
        instant dataAtualizacao
    }

    SUBMISSION {
        string id PK
        string examId FK
        string alunoId FK
        map respostas "questionId -> alternativaSelecionada"
        double nota
        boolean corrigida
        instant dataEnvio
    }

    CORRECTION_RESULT {
        string id PK
        string submissionId FK "unique"
        int acertos
        int erros
        double notaFinal
    }

    QUESTION_DETAIL {
        string questionId
        boolean correta
        string respostaAluno
        string respostaEsperada
        double pontuacaoObtida
    }

    EXAM_REPORT {
        string id PK
        string examId FK "unique"
        double mediaNotas
        double maiorNota
        double menorNota
        int totalSubmissoes
        instant dataGeracao
    }

    EXAM_STATISTICS {
        string id PK
        string examId FK "unique"
        map percentualAcertoPorQuestao "questionId -> %"
        map distribuicaoNotas "faixa -> count"
        list questoesComProblema
        instant dataGeracao
    }

    QUESTION_ISSUE {
        string id PK
        string questionId FK
        string examId FK
        string tipoProblema
        enum severidade "BAIXA | MEDIA | ALTA"
        string descricao
        enum geradoPor "MANUAL | AUTOMATICO"
        instant dataIdentificacao
    }

    SEED_METADATA {
        string id PK
        string seedKey UK
        instant executedAt
    }

    %% Relationships
    USER ||--o{ EXAM : "cria (professor)"
    EXAM ||--|{ QUESTION : "contém (embedded)"
    EXAM ||--o| ANSWER_KEY : "possui gabarito"
    EXAM ||--o{ SUBMISSION : "recebe"
    USER ||--o{ SUBMISSION : "envia (aluno)"
    SUBMISSION ||--o| CORRECTION_RESULT : "gera"
    CORRECTION_RESULT ||--|{ QUESTION_DETAIL : "detalha (embedded)"
    EXAM ||--o| EXAM_REPORT : "possui relatório"
    EXAM ||--o| EXAM_STATISTICS : "possui estatísticas"
    EXAM ||--o{ QUESTION_ISSUE : "tem problemas"
    QUESTION ||--o{ QUESTION_ISSUE : "reportado em"
```

## Notes

- `QUESTION` is an **embedded document** inside `EXAM` (no separate collection).
- `QUESTION_DETAIL` is an **embedded document** inside `CORRECTION_RESULT` (no separate collection).
- `SEED_METADATA` is an auxiliary collection used only for idempotent data seeding.
- The compound index `(examId, alunoId)` on `SUBMISSION` guarantees **one submission per student per exam**.
- `ANSWER_KEY`, `EXAM_REPORT`, and `EXAM_STATISTICS` each have a **unique index on `examId`** (one-to-one with Exam).
