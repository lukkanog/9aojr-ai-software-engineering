# Regras de Negócio e Domínio

---

## 1. Roles

O sistema possui dois roles:

- **PROFESSOR**: cria e gerencia provas, questões, gabaritos, relatórios e issues.
- **ALUNO**: realiza provas publicadas e consulta seus próprios resultados.

Um usuário possui exatamente um role, definido no cadastro. Não há troca de role.

---

## 2. Ciclo de Vida do Exam

O Exam possui três status, com transições unidirecionais:

```
RASCUNHO → PUBLICADA → ENCERRADA
```

### Regras de transição

| De         | Para       | Condições obrigatórias                                                                 |
|------------|------------|----------------------------------------------------------------------------------------|
| RASCUNHO   | PUBLICADA  | Apenas PROFESSOR dono do Exam. AnswerKey válido deve existir cobrindo todas as questões. Exam deve ter pelo menos 1 questão. |
| PUBLICADA  | ENCERRADA  | Apenas PROFESSOR dono do Exam. Transição é irreversível.                               |

- Não há transição reversa: `PUBLICADA → RASCUNHO` ou `ENCERRADA → PUBLICADA` são proibidas.
- Status `RASCUNHO`: Exam está em edição. Questões e gabarito podem ser alterados livremente.
- Status `PUBLICADA`: Exam está disponível para submissões. Questões e gabarito não podem mais ser editados.
- Status `ENCERRADA`: Exam não aceita novas submissões. Resultados e relatórios permanecem acessíveis.

---

## 3. Exam — Regras Gerais

- Apenas o **PROFESSOR** pode criar, editar e deletar Exams.
- Um PROFESSOR só pode editar/deletar Exams que ele próprio criou (ownership).
- Deletar um Exam é permitido apenas no status `RASCUNHO`.
- Editar título, descrição e configurações é permitido apenas no status `RASCUNHO`.
- `dataInicio` e `dataFim` são opcionais. Se definidos, representam a janela de submissão (ver seção Submissions).

---

## 4. Questions

- Questões são parte do documento Exam (subdocumento).
- Apenas o **PROFESSOR** dono do Exam pode criar, editar ou remover questões.
- Criação/edição/remoção de questões é permitida apenas quando o Exam está em `RASCUNHO`.
- Tipos de questão suportados:
  - `OBJETIVA`: múltipla escolha com exatamente 1 resposta correta.
  - `VERDADEIRO_FALSO`: duas alternativas (verdadeiro ou falso).
- Cada questão possui:
  - `enunciado`: texto da pergunta (obrigatório, não vazio).
  - `alternativas`: lista de opções (mínimo 2 para OBJETIVA; exatamente 2 para VERDADEIRO_FALSO).
  - `pontuacao`: valor numérico positivo atribuído à questão.
  - `ordem`: posição da questão na prova (inteiro, início em 1).

---

## 5. AnswerKey (Gabarito)

- Cada Exam possui **no máximo 1 AnswerKey**.
- Apenas o **PROFESSOR** dono do Exam pode criar ou atualizar o AnswerKey.
- O AnswerKey mapeia `questionId → alternativaCorreta` para cada questão do Exam.

### Regras de validação

- Um AnswerKey é considerado **válido** quando cobre **todas** as questões do Exam.
- Publicar o Exam requer AnswerKey válido (cobertura completa).

### Regra de imutabilidade após submissão

- **Após existir pelo menos 1 Submission associada ao Exam**, o AnswerKey **não pode ser alterado**.
- Tentativa de alteração nessa condição retorna erro `422` com código `ANSWER_KEY_LOCKED`.
- Enquanto o Exam estiver em `RASCUNHO` e sem submissions, o AnswerKey pode ser livremente atualizado.

---

## 6. Submissions

### Quem pode enviar

- Apenas usuários com role **ALUNO** podem enviar submissions.
- O ALUNO deve estar autenticado.

### Condições para aceitar uma submission

1. O Exam deve estar no status `PUBLICADA`.
2. Se `dataInicio` estiver definida: a data/hora atual deve ser ≥ `dataInicio`.
3. Se `dataFim` estiver definida: a data/hora atual deve ser ≤ `dataFim`.
4. O ALUNO não deve ter uma submission prévia para este Exam.

### Regra de tentativa única

- Cada ALUNO pode enviar **exatamente 1 submission** por Exam.
- Tentativa de reenvio retorna erro `409` (Conflict) com código `SUBMISSION_ALREADY_EXISTS`.

### Estrutura da Submission

- Contém mapa de `questionId → alternativaSelecionada`.
- O ALUNO não é obrigado a responder todas as questões. Questões sem resposta são tratadas como **branco** (pontuação = 0).
- Alternativas inválidas (que não existem na questão) são rejeitadas com erro `400`.

---

## 7. Correção Automática

### Quando ocorre

- Disparada via `POST /submissions/{id}/correct`.
- Apenas o PROFESSOR ou o sistema pode disparar a correção.

### Tipos suportados

- `OBJETIVA`: compara alternativa selecionada com alternativa correta do AnswerKey.
- `VERDADEIRO_FALSO`: mesma lógica de comparação.

### Cálculo da nota

- **Nota = soma das pontuações das questões respondidas corretamente.**
- Questão correta: alternativa do aluno == alternativa do gabarito → soma `pontuacao` da questão.
- Questão incorreta: alternativa do aluno ≠ alternativa do gabarito → 0 pontos.
- Questão sem resposta (branco): 0 pontos.
- **Não há meio acerto.** Não há penalização por erro (nota negativa).

### CorrectionResult

O resultado da correção registra obrigatoriamente:

| Campo               | Descrição                                                    |
|----------------------|--------------------------------------------------------------|
| `acertos`            | Número total de questões respondidas corretamente            |
| `erros`              | Número total de questões respondidas incorretamente          |
| `notaFinal`          | Soma das pontuações das questões corretas                    |
| `detalhesPorQuestao` | Lista com o detalhe de cada questão (ver abaixo)             |

### Detalhe por questão

Cada item de `detalhesPorQuestao` contém:

| Campo              | Descrição                                          |
|--------------------|-----------------------------------------------------|
| `questionId`       | ID da questão                                       |
| `correta`          | Boolean: se o aluno acertou                         |
| `respostaAluno`    | Alternativa selecionada pelo aluno (ou null se branco) |
| `respostaEsperada` | Alternativa correta segundo o gabarito              |
| `pontuacaoObtida`  | Pontuação obtida nesta questão (0 ou valor integral)|

---

## 8. Relatórios (ExamReport)

Gerado sob demanda para um Exam específico. Requer pelo menos 1 submission corrigida.

| Campo             | Descrição                                         |
|-------------------|---------------------------------------------------|
| `mediaNotas`      | Média aritmética das notas finais                 |
| `maiorNota`       | Maior nota dentre todas as submissions corrigidas |
| `menorNota`       | Menor nota dentre todas as submissions corrigidas |
| `totalSubmissoes` | Número total de submissions corrigidas            |
| `dataGeracao`     | Data/hora de geração do relatório                 |

- Apenas PROFESSOR pode acessar relatórios.

---

## 9. Estatísticas (ExamStatistics)

Gerado sob demanda para um Exam específico. Requer pelo menos 1 submission corrigida.

| Campo                        | Descrição                                                                  |
|------------------------------|----------------------------------------------------------------------------|
| `percentualAcertoPorQuestao` | Mapa de `questionId → percentual de acerto` (0.0 a 100.0)                 |
| `distribuicaoNotas`          | Distribuição de notas em faixas (ex.: 0-10%, 10-20%, ..., 90-100%)        |
| `questoesComProblema`        | Lista de IDs de questões que possuem QuestionIssues associados             |

- Apenas PROFESSOR pode acessar estatísticas.

---

## 10. QuestionIssue (Problemas em Questões)

### Geração automática pelo sistema

O sistema gera QuestionIssues automaticamente com base em heurísticas estatísticas **somente quando houver amostra mínima**.

**Constante configurável**: `MIN_SUBMISSIONS_FOR_ISSUE = 20`

| Tipo de problema        | Condição                                 | Severidade |
|-------------------------|------------------------------------------|------------|
| `MUITO_BAIXO_ACERTO`   | Taxa de acerto da questão < 30%          | `ALTA`     |
| `MUITO_ALTO_ACERTO`    | Taxa de acerto da questão > 95%          | `BAIXA`    |
| `ALTO_INDICE_BRANCO`   | Taxa de branco/sem resposta > 40%        | `MEDIA`    |

- Issues automáticos são gerados com `geradoPor = SISTEMA`.
- Issues automáticos são recalculados quando estatísticas são geradas. Não são duplicados: se já existe issue automático do mesmo tipo para a mesma questão, ele é atualizado (não cria novo).

### Registro manual pelo PROFESSOR

- O PROFESSOR pode criar QuestionIssue manualmente a qualquer momento para qualquer questão.
- Campos obrigatórios: `tipoProblema`, `severidade`, `descricao`.
- `tipoProblema` pode ser qualquer valor (não restrito aos tipos automáticos).
- `geradoPor = PROFESSOR`.

### Severidades

| Severidade | Significado                                          |
|------------|------------------------------------------------------|
| `BAIXA`    | Observação; não indica erro mas merece atenção       |
| `MEDIA`    | Indica possível problema na formulação da questão    |
| `ALTA`     | Indica problema sério que pode invalidar a questão   |

---

## 11. Permissões por Endpoint (RBAC)

### PROFESSOR

| Ação                                  | Endpoints                                                      |
|---------------------------------------|----------------------------------------------------------------|
| CRUD de Exams (próprios)              | `GET/POST/PUT/DELETE /exams`, `GET /exams/{id}`                |
| Publicar/Encerrar Exam                | `POST /exams/{id}/publish`, `POST /exams/{id}/close`           |
| CRUD de Questions (Exam próprio)      | `GET/POST /exams/{examId}/questions`, `PUT/DELETE /questions/{id}` |
| CRUD de AnswerKey (Exam próprio)      | `GET/POST/PUT /exams/{examId}/answer-key`                      |
| Ver submissions de Exams próprios     | `GET /exams/{examId}/submissions`, `GET /submissions/{id}`     |
| Disparar correção                     | `POST /submissions/{id}/correct`                               |
| Ver resultado de correção             | `GET /submissions/{id}/correction-result`                      |
| Acessar relatórios e estatísticas     | `GET /exams/{examId}/report`, `GET /exams/{examId}/statistics` |
| Registrar/consultar issues            | `GET/POST /questions/{questionId}/issues`                      |

### ALUNO

| Ação                                    | Endpoints                                      |
|-----------------------------------------|-------------------------------------------------|
| Listar Exams publicados                 | `GET /exams` (filtrado por status PUBLICADA)    |
| Ver detalhes de Exam publicado          | `GET /exams/{id}` (apenas PUBLICADA/ENCERRADA)  |
| Enviar submission (1 vez por Exam)      | `POST /exams/{examId}/submissions`              |
| Ver própria submission                  | `GET /submissions/{id}` (apenas própria)        |
| Ver resultado da própria correção       | `GET /submissions/{id}/correction-result` (apenas própria) |

### Regras de ownership

- PROFESSOR acessa apenas recursos de Exams que ele próprio criou.
- ALUNO acessa apenas suas próprias submissions e resultados.
- Tentativa de acessar recurso de outro usuário retorna `403`.
