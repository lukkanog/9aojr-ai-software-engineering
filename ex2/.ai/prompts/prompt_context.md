# Contexto do projeto (fonte única)

## Projeto
- **Nome:** Sistema de Correção Automática de Provas Simples  
- **Visão geral:** Sistema que permite a professores criar provas, gerenciar gabaritos, aplicar correção automática e gerar relatórios/análises estatísticas de desempenho de alunos.

## Roles
- **PROFESSOR:** cria/gerencia provas, questões, gabaritos e relatórios  
- **ALUNO:** realiza provas publicadas e vê resultados das próprias submissões

## Funcionalidades principais
- CRUD de Prova  
- CRUD de Questões  
- CRUD de Gabarito (AnswerKey)  
- Envio de respostas do aluno (Submission)  
- Correção automática (CorrectionResult)  
- Relatório e estatísticas por prova  
- Registro/consulta de problemas em questões (QuestionIssue)  
- Autenticação com roles de professor e aluno  

## Tecnologias (decididas)
- **Backend:** Java 21  
- **Build:** Maven  
- **Banco:** MongoDB  
- **Frontend:** React + TailwindCSS  

## Entidades principais
- **User**: (id, nome, email, senhaHash, role[PROFESSOR|ALUNO], ativo, dataCriacao)  
- **Exam**: (id, titulo, descricao, professorId, dataInicio?, dataFim?, status[RASCUNHO|PUBLICADA|ENCERRADA], configuracoes)  
- **Question**: (id, examId, enunciado, tipo[OBJETIVA|VERDADEIRO_FALSO], alternativas, pontuacao, ordem)  
- **AnswerKey**: (id, examId, respostas[questionId→alternativaCorreta], dataCriacao, dataAtualizacao)  
- **Submission**: (id, examId, alunoId, respostas[questionId→alternativaSelecionada], nota, corrigida, dataEnvio)  
- **CorrectionResult**: (id, submissionId, acertos, erros, notaFinal, detalhesPorQuestao)  
- **ExamReport**: (id, examId, mediaNotas, maiorNota, menorNota, totalSubmissoes, dataGeracao)  
- **ExamStatistics**: (id, examId, percentualAcertoPorQuestao, distribuicaoNotas, questoesComProblema)  
- **QuestionIssue**: (id, questionId, examId, tipoProblema, severidade, descricao, geradoPor[SISTEMA|PROFESSOR], dataIdentificacao)  

---

# Endpoints (contrato)

## Auth
- POST `/auth/login`
- POST `/auth/logout`
- POST `/auth/register`
- GET `/auth/me`

## Users
- GET `/users`
- GET `/users/{id}`
- POST `/users`
- PUT `/users/{id}`
- DELETE `/users/{id}`

## Exams
- GET `/exams`
- GET `/exams/{id}`
- POST `/exams`
- PUT `/exams/{id}`
- DELETE `/exams/{id}`
- POST `/exams/{id}/publish`
- POST `/exams/{id}/close`

## Questions
- GET `/exams/{examId}/questions`
- POST `/exams/{examId}/questions`
- PUT `/questions/{id}`
- DELETE `/questions/{id}`

## AnswerKey
- GET `/exams/{examId}/answer-key`
- POST `/exams/{examId}/answer-key`
- PUT `/exams/{examId}/answer-key`

## Submissions
- GET `/exams/{examId}/submissions`
- GET `/submissions/{id}`
- POST `/exams/{examId}/submissions`

## Correction
- POST `/submissions/{id}/correct`
- GET `/submissions/{id}/correction-result`

## Reports & Stats
- GET `/exams/{examId}/report`
- GET `/exams/{examId}/statistics`

## Question Issues
- GET `/questions/{questionId}/issues`
- POST `/questions/{questionId}/issues`

---

# Observações arquiteturais (diretrizes)
- MongoDB favorece documentos agregados (ex.: questões dentro de Exam quando fizer sentido)
- Endpoints devem respeitar autorização baseada em role
- Correção automática inicia síncrona; arquitetura deve permitir evolução para assíncrona
- Relatórios/estatísticas podem ser sob demanda inicialmente, com evolução para pré-cálculo/cache

---

# Conteúdo esperado (DETALHADO)

## 1) .ai/standards.md — Padrões de código e estilo

Defina regras concretas para backend (Java 21) e frontend (React).  
**Proibir frases vagas. Tudo deve ser prescritivo.**

### Backend (Java 21 + Maven)
- **Java:** 21  
  - Usar recursos modernos com parcimônia: `record`, `sealed` (se necessário), etc.
- **Build:** Maven (padrão do projeto)
- **Framework:** usar um framework moderno compatível com Java 21 (ex.: Spring Boot 3.x+ se adotado)

**Estilo e linguagem**
- DTOs preferencialmente como `record` quando fizer sentido
- `Optional` apenas em retornos (nunca em campos)
- Stream API apenas para transformações simples e legíveis

**Convenções REST**
- Controllers finos (request/response + validação + roteamento)
- Regras de negócio em `Service`
- Persistência em `Repository`
- Rotas consistentes, substantivos e plural:
  - `/exams`, `/submissions`, etc.
- Status codes padronizados:
  - `200/201/204/400/401/403/404/409/422/500`
- Validações:
  - `jakarta.validation` obrigatório
- Erros:
  - resposta única e consistente (ex.: `code`, `message`, `details`, `traceId`)

**Logs**
- Sempre logar `traceId`/`correlationId`
- Nunca logar senha, tokens ou PII desnecessária

**Organização mínima de pacotes (obrigatório, não misturar)**
- `controller`
- `service`
- `repository`
- `domain`
- `dto`
- `config`
- `security`
- `error`

**MongoDB (modelagem)**
- `Exam` pode agregar `Question` (caso o volume/uso favoreça leitura)
- `Submission` e `CorrectionResult` preferencialmente em coleções separadas (alto volume)

**Testes**
- Mínimo:
  - unit tests para regras de correção
  - unit tests para regras de status da prova
- (Opcional) integração com Mongo via container, se adotado

### Frontend (React + TailwindCSS)

**Componentização**
- Componentes pequenos, reutilizáveis e nomeados consistentemente
- Separar páginas (views) de componentes

**Estado**
- Preferir estado local
- Não introduzir gerenciamento global sem necessidade

**Acessibilidade e responsividade**
- Responsivo obrigatório (desktop/mobile)
- Inputs com label, foco visível e navegação por teclado

**Consumo de API**
- Camada `api/` centralizada
- Tratamento padrão de erro (mensagens amigáveis)

---

## 2) .ai/architecture.md — Arquitetura e decisões

Descreva explicitamente:
- Camadas: React → API REST Java 21 → MongoDB
- RBAC com roles `PROFESSOR`/`ALUNO` em todos endpoints relevantes
- Correção automática síncrona inicialmente, desenhando para suportar async no futuro
- Estratégia para relatórios/estatísticas:
  - on-demand inicialmente
  - evolução para pré-cálculo/cache
- Decisões de modelagem Mongo (agregados e coleções)

Incluir **5–10 decisões** no formato:
- Decision:
- Context:
- Consequence:

As decisões devem orientar implementação sem inventar arquitetura.

---

## 3) .ai/tech-stack.md — Stack tecnológica (PRESCRITIVO)

Defina explicitamente tecnologias aprovadas.

### Backend
- Java 21
- Build: Maven
- Framework recomendado (compatível com Java 21): Spring Boot 3.x+ (se adotado)
- MongoDB driver/framework: via framework escolhido (ex.: starter Mongo)
- Documentação de API: OpenAPI/Swagger (via lib compatível com o framework)
- Segurança: mecanismo de autenticação e autorização (RBAC) compatível com o framework

### Frontend
- React
- TailwindCSS
- (Opcional) Router e libs auxiliares apenas se houver justificativa técnica e consistência

### Banco
- MongoDB

**Regra**
- Qualquer tecnologia fora desta lista deve ser tratada como **não aprovada** sem decisão registrada em `.ai/architecture.md`.

---

## 4) .ai/business-rules.md — Regras de negócio e domínio

Documente regras claras e operacionais. **Não incluir código. Apenas regras.**

### Roles
- `PROFESSOR`
- `ALUNO`

### Ciclo de vida Exam
- `RASCUNHO` → `PUBLICADA` → `ENCERRADA`

### Regras (obrigatórias)
- Apenas `PROFESSOR` cria/edita `Exam` e `Question`
- Apenas `Exam` `PUBLICADA` aceita `Submission`
- `Exam` `ENCERRADA` não aceita novas `Submissions`
- Se `dataInicio`/`dataFim` existirem:
  - submissões somente dentro da janela (inclusive)

### AnswerKey (OBRIGATÓRIO)
- Apenas `PROFESSOR` cria/atualiza
- Obrigatório para publicar:
  - um `Exam` só pode ir para `PUBLICADA` se existir `AnswerKey` válido cobrindo todas as questões publicadas
- Alterar `AnswerKey` após existir `Submission`:
  - deve ser proibido **OU** gerar nova versão
  - decidir e registrar no documento
  - padrão recomendado: proibir após primeira submissão

### Submissions (1 tentativa)
- Apenas `ALUNO` envia submission
- Regra de tentativas:
  - exatamente 1 tentativa por aluno por prova
  - um segundo POST deve retornar `409` (conflito) **ou** `422` (regra de negócio)

### Correção automática
- Tipos suportados: `OBJETIVA` e `VERDADEIRO_FALSO`
- Nota:
  - soma das pontuações das questões corretas
  - questão sem resposta = 0
  - sem meio acerto
- `CorrectionResult` deve registrar:
  - acertos, erros, notaFinal
  - detalhe por questão (correta/errada, resposta do aluno, resposta esperada)

### Relatórios e Estatísticas
**ExamReport (mínimo)**
- média, maior, menor nota, total submissões, data geração

**ExamStatistics (mínimo)**
- percentual de acerto por questão
- distribuição de notas (faixas)
- lista de questões com problema

### QuestionIssue — heurísticas sugeridas (SISTEMA) + manual (PROFESSOR)

**Geração automática (SISTEMA)**
- Só gerar issues com amostra mínima:
  - `MIN_SUBMISSIONS_FOR_ISSUE = 20` (configurável)
- Tipos sugeridos (exemplos):
  - `MUITO_BAIXO_ACERTO`
  - `MUITO_ALTO_ACERTO`
  - `ALTO_INDICE_BRANCO` (sem resposta)
  - `DIVERGENCIA_COMPORTAMENTO` (variação extrema entre turmas, se houver esse dado)
- Regras sugeridas (configuráveis):
  - MUITO_BAIXO_ACERTO: taxa de acerto < 30%
  - MUITO_ALTO_ACERTO: taxa de acerto > 95%
  - ALTO_INDICE_BRANCO: branco/sem resposta > 40%
- Severidade sugerida:
  - BAIXA: muito alto acerto
  - MEDIA: alto índice branco
  - ALTA: muito baixo acerto

**Registro manual (PROFESSOR)**
- `PROFESSOR` pode criar `QuestionIssue` manualmente sempre
- Deve incluir descrição livre e tipo/severidade

### Permissões (RBAC)

**PROFESSOR**
- CRUD exams/questions/answer-key
- publish/close
- ver submissions e resultados
- acessar report/statistics
- registrar/consultar issues

**ALUNO**
- listar exams disponíveis/publicados
- enviar submission (1x)
- ver resultado da própria submission

---

# Regras finais
- Escreva os documentos pensando em outro agente
- Seja específico e direto
- Não use frases genéricas
- Não antecipe implementação (somente regras e decisões)

---

# Entregável
- Pasta `.ai/` criada
- Quatro arquivos preenchidos conforme especificado
- Ao final, apresentar:
  - Lista dos arquivos criados
  - Um resumo objetivo de cada documento
