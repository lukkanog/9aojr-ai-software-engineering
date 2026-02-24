# Arquitetura e Decisões

---

## Visão Geral da Arquitetura

```
┌──────────────┐       HTTP/JSON        ┌──────────────────┐       MongoDB Driver       ┌──────────┐
│   Frontend   │  ◄──────────────────►  │   Backend API    │  ◄──────────────────────►  │ MongoDB  │
│  React + TW  │                        │  Java 21 + SB3   │                            │          │
└──────────────┘                        └──────────────────┘                            └──────────┘
```

### Camadas

1. **Frontend (React + TailwindCSS)**: SPA que consome a API REST. Responsável por renderização, navegação e interação do usuário.
2. **API REST (Java 21 + Spring Boot 3.x)**: Backend stateless que expõe endpoints REST. Responsável por autenticação, autorização, regras de negócio e persistência.
3. **MongoDB**: Banco de dados orientado a documentos. Armazena todas as entidades do sistema.

### Fluxo de Comunicação

- Frontend faz requisições HTTP (JSON) para o backend.
- Backend autentica via JWT, verifica role (RBAC) e processa a requisição.
- Backend acessa MongoDB via Spring Data MongoDB.
- Respostas são sempre JSON com status codes padronizados.

---

## Autorização (RBAC)

O sistema implementa RBAC com dois roles:

| Role       | Permissões                                                                                       |
|------------|--------------------------------------------------------------------------------------------------|
| PROFESSOR  | CRUD exams, questions, answer-key; publish/close exam; ver submissions/resultados; reports/stats; issues |
| ALUNO      | Listar exams publicados; enviar 1 submission por exam; ver resultado da própria submission        |

- Autorização é verificada em **todo endpoint protegido**, no nível do Controller (via anotações de segurança) e no Service (verificação de ownership quando aplicável).
- Endpoints públicos: `/auth/login`, `/auth/register`.
- Todos os demais endpoints requerem autenticação.

---

## Autenticação

- Autenticação via **JWT (JSON Web Token)**.
- Token é emitido no login e contém: `userId`, `email`, `role`.
- Token é enviado no header `Authorization: Bearer <token>` em toda requisição autenticada.
- Backend valida token em um filtro de segurança antes de chegar ao Controller.
- Logout é tratado no cliente (descarte do token). O backend não mantém sessão.

---

## Modelagem MongoDB

### Coleções

| Coleção             | Justificativa                                                                                     |
|---------------------|---------------------------------------------------------------------------------------------------|
| `users`             | Coleção independente. Baixo volume, consultas por email e id.                                     |
| `exams`             | Documento agregado que **inclui as questões (questions)** como subdocumento. Favorece leitura completa da prova em uma única consulta. |
| `answer_keys`       | Coleção separada. Vinculada ao exam por `examId`. Separação facilita validação independente.      |
| `submissions`       | Coleção separada. Alto volume esperado. Índice composto em `(examId, alunoId)` para garantir unicidade. |
| `correction_results`| Coleção separada. Vinculada à submission por `submissionId`. Separação permite evolução para processamento assíncrono. |
| `exam_reports`      | Coleção separada (ou gerada sob demanda). Armazena relatórios calculados.                         |
| `exam_statistics`   | Coleção separada (ou gerada sob demanda). Armazena estatísticas calculadas.                       |
| `question_issues`   | Coleção separada. Vinculada por `questionId` e `examId`. Permite consulta independente.           |

### Decisão: Questions como subdocumento de Exam

- Questions são sempre lidas junto com o Exam.
- O volume de questões por prova é baixo (dezenas, não milhares).
- Simplifica consultas e garante consistência.
- A `ordem` da questão é mantida no array.

---

## Correção Automática

- **Fase inicial**: correção síncrona. Ao receber `POST /submissions/{id}/correct`, o backend calcula a nota imediatamente e retorna o resultado.
- **Preparação para assíncrono**: a interface do Service de correção deve ser desacoplada (`CorrectionService`) para permitir substituição futura por processamento via fila/evento sem alterar o Controller.
- O `CorrectionResult` é persistido em coleção separada, o que facilita a evolução para workers assíncronos.

---

## Relatórios e Estatísticas

- **Fase inicial**: geração sob demanda. Ao chamar `GET /exams/{examId}/report` ou `/statistics`, o backend calcula com base nas submissions existentes.
- **Evolução futura**: pré-cálculo e cache. A interface do Service deve permitir que o cálculo seja substituído por leitura de documento pré-calculado sem alterar o Controller.
- Relatórios e estatísticas são persistidos após geração para evitar recálculo desnecessário em chamadas subsequentes (cache simples com invalidação manual ou por TTL).

---

## Registro de Decisões Arquiteturais (ADRs)

### ADR-001: Framework Backend — Spring Boot 3.x

- **Decision**: Adotar Spring Boot 3.x como framework backend.
- **Context**: O projeto precisa de um framework Java 21 compatível, com ecossistema maduro para REST, segurança e MongoDB. Spring Boot 3.x tem suporte nativo a Java 21 e oferece starters para todas as necessidades do projeto.
- **Consequence**: O projeto depende do ecossistema Spring (Spring Web, Spring Security, Spring Data MongoDB). Desenvolvedores precisam conhecer convenções Spring.

---

### ADR-002: Autenticação via JWT stateless

- **Decision**: Usar JWT para autenticação, sem sessão no servidor.
- **Context**: O backend é uma API REST consumida por um SPA React. Sessões server-side adicionam complexidade desnecessária. JWT permite autenticação stateless e escala horizontal sem compartilhamento de sessão.
- **Consequence**: Logout é client-side (descarte do token). Revogação de token em tempo real não é possível sem infraestrutura adicional (blacklist). Aceita-se essa limitação na fase inicial.

---

### ADR-003: Questions como subdocumento de Exam

- **Decision**: Armazenar questões como array dentro do documento Exam no MongoDB.
- **Context**: Questões são sempre lidas junto com a prova. O volume por prova é baixo. Manter em coleção separada adicionaria JOINs desnecessários (lookups) em um banco orientado a documentos.
- **Consequence**: Atualizações individuais de questão exigem update no documento do Exam. O tamanho do documento Exam cresce com o número de questões (aceitável para dezenas de questões).

---

### ADR-004: Submissions e CorrectionResults em coleções separadas

- **Decision**: Manter Submission e CorrectionResult em coleções independentes.
- **Context**: Submissions podem ter alto volume. Separar permite indexação independente, consultas especializadas e evolução para correção assíncrona. CorrectionResult em coleção separada permite que workers independentes escrevam resultados sem conflito com o documento de submission.
- **Consequence**: Consultas que precisam de submission + resultado exigem duas queries ou lookup. Aceita-se em troca de flexibilidade e escalabilidade.

---

### ADR-005: Correção síncrona com design para assíncrono

- **Decision**: Implementar correção automática de forma síncrona inicialmente, mas com interface desacoplada no Service.
- **Context**: A complexidade de filas/mensageria não se justifica no MVP. Porém, o design deve permitir evolução sem refatoração do Controller.
- **Consequence**: Na fase inicial, a resposta ao `POST /submissions/{id}/correct` é síncrona. O `CorrectionService` é uma interface que pode ser substituída por implementação assíncrona no futuro.

---

### ADR-006: Relatórios sob demanda com cache simples

- **Decision**: Gerar relatórios e estatísticas sob demanda, com persistência do resultado para cache.
- **Context**: Pré-cálculo em tempo real requer infraestrutura de eventos/cron que não se justifica no MVP. Calcular sob demanda e persistir o resultado oferece equilíbrio entre simplicidade e performance.
- **Consequence**: A primeira chamada ao relatório pode ser lenta se houver muitas submissions. Chamadas subsequentes leem o documento salvo. Invalidação do cache deve ser tratada quando novas submissions forem adicionadas.

---

### ADR-007: RBAC no nível de anotação Spring Security

- **Decision**: Implementar autorização via anotações `@PreAuthorize` nos Controllers, combinada com verificações de ownership no Service.
- **Context**: O sistema tem apenas dois roles simples. Anotações declarativas são suficientes e mantêm a autorização explícita e auditável no código.
- **Consequence**: Mudanças de permissão exigem alteração no código-fonte. Aceita-se essa limitação dado a simplicidade do modelo de roles.

---

### ADR-008: AnswerKey proibido de alteração após primeira Submission

- **Decision**: Proibir alteração do AnswerKey de um Exam após existir pelo menos uma Submission associada.
- **Context**: Alterar o gabarito após submissões invalidaria correções já realizadas. Versionar gabaritos adiciona complexidade significativa.
- **Consequence**: Se o professor identificar erro no gabarito após submissões, deve criar novo Exam. Essa restrição é comunicada claramente na API (erro 422).

---

### ADR-009: Tentativa única de Submission por aluno por Exam

- **Decision**: Cada aluno pode enviar exatamente 1 submission por exam.
- **Context**: O sistema modela provas formais onde retentativa não é permitida. A unicidade é garantida por índice composto `(examId, alunoId)` no MongoDB.
- **Consequence**: Reenvio retorna erro 409 (Conflict). Não há mecanismo de anulação de tentativa no MVP.

---

### ADR-010: Frontend SPA com React Router

- **Decision**: Usar React Router para navegação client-side no SPA.
- **Context**: O frontend tem múltiplas telas (login, lista de exams, detalhes, submissions, relatórios). React Router é a solução padrão do ecossistema React para roteamento client-side.
- **Consequence**: Navegação é client-side. O servidor deve servir `index.html` para todas as rotas (fallback para SPA). Deep linking funciona via React Router.
