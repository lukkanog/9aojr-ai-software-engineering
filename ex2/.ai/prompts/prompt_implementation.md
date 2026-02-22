
Todas as decisões técnicas, arquiteturais e de negócio **DEVEM** seguir exatamente o que está definido nesses documentos.

⚠️ Se houver conflito entre este prompt e os arquivos `.ai/`, **os arquivos `.ai/` têm prioridade**.

---

## Objetivo

Implementar um sistema completo com:

* **Backend**: API REST em **Java 21 + Spring Boot 3.x + Maven**
* **Banco**: **MongoDB**
* **Frontend**: SPA em **React + TailwindCSS + Vite + React Router**

A aplicação deve ser:

* determinística
* previsível
* consumível por um frontend sem adaptações
* consistente com RBAC (PROFESSOR/ALUNO), regras de domínio e padrões de erro

---

## Escopo funcional (FECHADO)

O sistema possui **exatamente** os endpoints abaixo (não criar endpoints extras fora da lista):

**Auth**
* `POST /auth/login`
* `POST /auth/logout`
* `POST /auth/register`
* `GET /auth/me`

**Users**
* `GET /users`
* `GET /users/{id}`
* `POST /users`
* `PUT /users/{id}`
* `DELETE /users/{id}`

**Exams**
* `GET /exams`
* `GET /exams/{id}`
* `POST /exams`
* `PUT /exams/{id}`
* `DELETE /exams/{id}`
* `POST /exams/{id}/publish`
* `POST /exams/{id}/close`

**Questions**
* `GET /exams/{examId}/questions`
* `POST /exams/{examId}/questions`
* `PUT /questions/{id}`
* `DELETE /questions/{id}`

**AnswerKey**
* `GET /exams/{examId}/answer-key`
* `POST /exams/{examId}/answer-key`
* `PUT /exams/{examId}/answer-key`

**Submissions**
* `GET /exams/{examId}/submissions`
* `GET /submissions/{id}`
* `POST /exams/{examId}/submissions`

**Correction**
* `POST /submissions/{id}/correct`
* `GET /submissions/{id}/correction-result`

**Reports & Stats**
* `GET /exams/{examId}/report`
* `GET /exams/{examId}/statistics`

**Question Issues**
* `GET /questions/{questionId}/issues`
* `POST /questions/{questionId}/issues`

Nenhum outro endpoint deve ser criado.

---

## ️ Etapas obrigatórias de implementação

Execute o trabalho **nesta ordem**, sem pular etapas:

### Etapa 1 — Estrutura do repositório (fullstack)

* Criar estrutura padrão:
/
├── backend/
├── frontend/
├── docker-compose.yml
└── README.md

* `docker-compose.yml`: subir MongoDB para dev local
* `README.md`: instruções claras de execução (mongo + backend + frontend)

---

### Etapa 2 — Bootstrap do Backend (Java 21 + Maven + Spring Boot 3.x)

* Criar projeto Spring Boot compatível com **Java 21**
* Usar **Maven** (proibido Gradle)
* Configurar `application.yml` (MongoDB, JWT, CORS, etc.)
* Organizar pacotes conforme `.ai/standards.md` (base package: `com.projeto.examcorrection`)
* Configurar Swagger/OpenAPI via SpringDoc

---

### Etapa 3 — Modelagem de domínio + MongoDB (coleções e agregados)

Implementar domínio e persistência seguindo `.ai/architecture.md`:

* Coleções:
* `users`
* `exams` (**questions como subdocumento dentro de exam**)
* `answer_keys`
* `submissions` (com índice composto único `(examId, alunoId)`)
* `correction_results`
* `exam_reports`
* `exam_statistics`
* `question_issues`

* Criar:
* `domain/` (documentos Mongo)
* `repository/` (Spring Data MongoDB)
* `dto/` (requests/responses como `record`, salvo exceções justificadas)

---

### Etapa 4 — Segurança (JWT + RBAC)

* Implementar autenticação via **JWT stateless**
* Token contém: `userId`, `email`, `role`
* Header: `Authorization: Bearer <token>`
* Implementar:
* `POST /auth/register`
* `POST /auth/login`
* `GET /auth/me`
* `POST /auth/logout` (logout client-side; backend pode responder 204/200)
* Proteger todos endpoints exceto `/auth/login` e `/auth/register`
* Implementar RBAC:
* `@PreAuthorize` nos Controllers
* checks de ownership no Service (professor só acessa exams próprios; aluno só acessa submissions próprias)

---

### Etapa 5 — Exams + Questions (CRUD + regras de status)

* Implementar CRUD de `Exam` com regras:
* Apenas **PROFESSOR** cria/edita/deleta
* Editar/deletar somente quando `status = RASCUNHO`
* Transições: `RASCUNHO → PUBLICADA → ENCERRADA` (irreversível)
* `publish` exige:
  * exam com **pelo menos 1 questão**
  * **AnswerKey válido cobrindo todas as questões**
* Implementar Questions como **subdocumento** do Exam:
* `GET/POST /exams/{examId}/questions`
* `PUT/DELETE /questions/{id}` (atualiza/remover dentro do Exam dono)
* Alterações só em `RASCUNHO`

---

### Etapa 6 — AnswerKey (obrigatório + lock após submissão)

* Implementar:
* `GET/POST/PUT /exams/{examId}/answer-key`
* Regras obrigatórias:
* Cada Exam tem **no máximo 1 AnswerKey**
* AnswerKey válido = cobre **todas** as questões do Exam
* Após existir pelo menos 1 `Submission`:
  * **proibir alteração do AnswerKey**
  * retornar `422` com `code = ANSWER_KEY_LOCKED`

---

### Etapa 7 — Submissions (1 tentativa + validações + janela)

* Implementar:
* `POST /exams/{examId}/submissions`
* `GET /submissions/{id}`
* `GET /exams/{examId}/submissions`
* Regras obrigatórias:
* Apenas **ALUNO** envia submission
* Exam deve estar `PUBLICADA`
* Respeitar janela se existir:
  * agora ≥ `dataInicio` (se definida)
  * agora ≤ `dataFim` (se definida)
* **Exatamente 1 submission por aluno por exam**:
  * reenvio retorna `409` com `code = SUBMISSION_ALREADY_EXISTS`
* Alternativa inválida → `400`
* Branco é permitido e vale 0

---

### Etapa 8 — Correção automática (síncrona agora, preparada para async)

* Implementar:
* `POST /submissions/{id}/correct`
* `GET /submissions/{id}/correction-result`
* Regras obrigatórias:
* Nota = soma das pontuações das questões corretas
* Sem meio acerto; sem penalidade por erro
* Persistir `CorrectionResult` em `correction_results`
* Atualizar `Submission` (`corrigida`, `nota`)
* Arquitetura obrigatória:
* Service desacoplado (`CorrectionService`) para suportar evolução assíncrona

---

### Etapa 9 — Relatórios e estatísticas (sob demanda + cache simples)

* Implementar:
* `GET /exams/{examId}/report`
* `GET /exams/{examId}/statistics`
* Regras:
* Requer pelo menos 1 submission **corrigida**
* Gerar sob demanda e **persistir** como cache:
  * `exam_reports`
  * `exam_statistics`
* Invalidação do cache:
  * ao corrigir uma nova submission do exam, invalidar report/statistics daquele exam (remover ou recalcular)

---

### Etapa 10 — QuestionIssue (automático + manual)

* Implementar:
* `GET /questions/{questionId}/issues`
* `POST /questions/{questionId}/issues`

* Geração automática (quando estatísticas são geradas):
* apenas se `MIN_SUBMISSIONS_FOR_ISSUE = 20`
* tipos e thresholds:
  * `MUITO_BAIXO_ACERTO`: acerto < 30% → severidade `ALTA`
  * `MUITO_ALTO_ACERTO`: acerto > 95% → severidade `BAIXA`
  * `ALTO_INDICE_BRANCO`: branco > 40% → severidade `MEDIA`
* não duplicar:
  * se já existir issue automático do mesmo tipo para a mesma questão, atualizar

* Manual (PROFESSOR):
* pode criar issue a qualquer momento
* campos obrigatórios: `tipoProblema`, `severidade`, `descricao`
* `geradoPor = PROFESSOR`

---

### Etapa 11 — Tratamento de erros (padrão único + status codes)

* Implementar `@RestControllerAdvice` global
* Toda resposta de erro deve seguir o formato padronizado do `.ai/standards.md`
* Garantir status codes conforme padrão:
* 400, 401, 403, 404, 409, 422, 500
* Incluir `traceId` em todas as respostas de erro + logs

---

### Etapa 12 — Frontend (React + TailwindCSS + Vite + Router)

* Criar SPA com Vite + React Router + Tailwind
* Regras obrigatórias:
* componentes pequenos e reutilizáveis
* separar `pages/` e `components/`
* camada `api/` centralizada para chamadas HTTP
* 401 → redirecionar login
* 403 → mensagem de permissão
* responsivo e acessível (labels, foco visível, teclado)

* Fluxos mínimos (MVP):
* **ALUNO**:
  * login
  * listar exams publicados
  * ver detalhes do exam
  * enviar submission (1 tentativa)
  * ver correction-result da própria submission
* **PROFESSOR**:
  * login
  * CRUD exam (rascunho)
  * CRUD questions (rascunho)
  * criar/editar answer-key (antes de submissions)
  * publish/close
  * listar submissions do exam
  * disparar correção
  * ver report/statistics
  * ver/criar issues manualmente

---

### Etapa 13 — Documentação + README final

* Swagger/OpenAPI acessível e refletindo os endpoints do escopo fechado
* README com:
* setup (docker compose, backend, frontend)
* variáveis/configs necessárias
* como autenticar e testar rapidamente (curl ou exemplos simples)

---

### Etapa 14 — Testes mínimos obrigatórios

Implementar testes unitários (JUnit 5 + Mockito) para:

* regras de correção automática
* regras de status do exam (publish/close e restrições de edição)
* AnswerKey obrigatório + lock após primeira submission
* tentativa única de submission (conflito 409)

---

## Formato de resposta (OBRIGATÓRIO)

Todas as respostas são **JSON**.

### Erro (formato obrigatório)

Toda resposta de erro **DEVE** seguir este formato:

```json
{
"code": "EXAM_NOT_FOUND",
"message": "Prova não encontrada com o ID informado.",
"details": null,
"traceId": "abc123-def456"
}