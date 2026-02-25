# Sequence Diagram — `POST /exams/{examId}/submissions`

> **Role required**: `ALUNO`
> **Auth**: JWT Bearer Token (Spring Security)

```mermaid
sequenceDiagram
    autonumber

    actor Aluno as Aluno (Client)
    participant SC as SubmissionController
    participant SS as SubmissionService
    participant US as UserService
    participant ES as ExamService
    participant SubmRepo as SubmissionRepository
    participant UserRepo as UserRepository
    participant ExamRepo as ExamRepository
    participant DB as MongoDB

    Aluno->>SC: POST /exams/{examId}/submissions<br/>Authorization: Bearer <token><br/>Body: { respostas: { questionId: alternativa } }

    Note over SC: Spring Security validates JWT<br/>and checks role == ALUNO

    SC->>SS: create(examId, request, principal.getName())

    SS->>ES: findById(examId)
    ES->>ExamRepo: findById(examId)
    ExamRepo->>DB: db.exams.findOne({ _id: examId })
    DB-->>ExamRepo: Exam document
    ExamRepo-->>ES: Optional<Exam>
    ES-->>SS: Exam

    alt Exam status != PUBLICADA
        SS-->>SC: throw BusinessRuleException(EXAM_NOT_PUBLISHED)
        SC-->>Aluno: 422 Unprocessable Entity
    end

    alt now < dataInicio
        SS-->>SC: throw BusinessRuleException(EXAM_NOT_STARTED)
        SC-->>Aluno: 422 Unprocessable Entity
    end

    alt now > dataFim
        SS-->>SC: throw BusinessRuleException(EXAM_EXPIRED)
        SC-->>Aluno: 422 Unprocessable Entity
    end

    SS->>SubmRepo: existsByExamIdAndAlunoId(examId, alunoId)
    SubmRepo->>DB: db.submissions.findOne({ examId, alunoId })
    DB-->>SubmRepo: document | null
    SubmRepo-->>SS: boolean

    alt Submission already exists
        SS-->>SC: throw ConflictException(SUBMISSION_ALREADY_EXISTS)
        SC-->>Aluno: 409 Conflict
    end

    Note over SS: Validate each answer in request:<br/>- questionId must belong to the exam<br/>- alternativa must be in question.alternativas

    alt Invalid questionId or alternativa
        SS-->>SC: throw BusinessRuleException(INVALID_QUESTION_ID / INVALID_ALTERNATIVE)
        SC-->>Aluno: 422 Unprocessable Entity
    end

    SS->>SubmRepo: save(newSubmission)
    SubmRepo->>DB: db.submissions.insertOne({...})
    DB-->>SubmRepo: Saved Submission
    SubmRepo-->>SS: Submission

    SS->>UserRepo: findById(alunoId)
    UserRepo->>DB: db.users.findOne({ _id: alunoId })
    DB-->>UserRepo: User document
    UserRepo-->>SS: Optional<User>

    SS-->>SC: SubmissionResponse
    SC-->>Aluno: 201 Created<br/>{ id, examId, alunoId, alunoNome, respostas, nota: null, corrigida: false, dataEnvio }
```

## Response Body (201 Created)

```json
{
  "id": "string",
  "examId": "string",
  "alunoId": "string",
  "alunoNome": "string",
  "respostas": {
    "<questionId>": "<alternativaSelecionada>"
  },
  "nota": null,
  "corrigida": false,
  "dataEnvio": "2024-01-01T10:00:00Z"
}
```

## Business Rules

| Rule | Error Code | HTTP Status |
|------|-----------|-------------|
| Exam must have status `PUBLICADA` | `EXAM_NOT_PUBLISHED` | 422 |
| Current time must be after `dataInicio` | `EXAM_NOT_STARTED` | 422 |
| Current time must be before `dataFim` | `EXAM_EXPIRED` | 422 |
| Student can only submit once per exam | `SUBMISSION_ALREADY_EXISTS` | 409 |
| All `questionId` keys must belong to the exam | `INVALID_QUESTION_ID` | 422 |
| Each answer must be a valid alternative for its question | `INVALID_ALTERNATIVE` | 422 |
| Only users with role `ALUNO` can submit | — | 403 |
