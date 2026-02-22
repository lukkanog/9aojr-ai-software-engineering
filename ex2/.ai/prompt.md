# Conteúdo esperado (DETALHADO)

## 1) .ai/standards.md — Padrões de código e estilo

Defina regras concretas para **backend (Java)** e **frontend (React)**.

### Backend (Java)

**Java**
- Versão mínima: **TBD**
- Se não houver decisão explícita: adotar **LTS** como referência e **registrar isso como decisão**

**Estilo e linguagem**
- DTOs imutáveis, preferencialmente como `record`
- `Optional` apenas em retornos (nunca em campos)
- Stream API apenas para filtros/ordenações simples (sem abusar e sem perder legibilidade)

**Convenções REST**
- Controllers finos (apenas request/response + validação + roteamento)
- Lógica de negócio apenas em `Service`
- Persistência apenas em `Repository`
- Endpoints consistentes:
  - pluralização correta
  - verbos HTTP corretos
  - sub-recursos consistentes
- Status codes padronizados: `200/201/204/400/401/403/404/409/422/500`
- Validações: `jakarta.validation` (obrigatório)
- Erros: padrão único de resposta (ex.: `code`, `message`, `details`, `traceId`)

**Logs**
- Logar `traceId`/`correlationId`
- Não logar senha/token/dados sensíveis

**Organização de pacotes (mínimo)**
- `controller`
- `service`
- `repository`
- `domain`
- `dto`
- `config`
- `security`
- `error`

**Regras MongoDB (modelagem)**
- Preferir agregados coerentes (ex.: questões agregadas em `Exam` quando fizer sentido)
- Evitar “JOIN mental”: não espalhar domínio sem necessidade
- `Submission` e `CorrectionResult` podem ser coleções separadas (alto volume)

**Testes**
- Desejáveis (mínimo: unit tests de regras de correção)
- Se houver testes de integração, devem rodar localmente (Mongo via container é aceitável)

---

### Frontend (React + TailwindCSS)

**Componentização**
- Componentes reutilizáveis, focados e com nomes consistentes
- Separar páginas (views) de componentes

**Estado**
- Preferir estado local; não introduzir gerenciadores globais sem necessidade

**Acessibilidade e responsividade**
- Layout responsivo obrigatório
- Inputs com labels, foco visível e navegação por teclado

**Consumo de API**
- Camada `api/` centralizada
- Tratamento padrão de erro com mensagens amigáveis

**Regra de linguagem**
- Evite frases vagas como “quando possível”.
- Defina explicitamente o que é permitido e o que é proibido.

---

## 2) .ai/architecture.md — Arquitetura e decisões

Descreva explicitamente:

**Arquitetura em camadas**
- Frontend (React) → Backend (API REST Java) → MongoDB

**RBAC**
- Endpoints com autorização por role (`PROFESSOR`/`ALUNO`)

**Estratégia de correção**
- Correção automática inicialmente síncrona
- Evolução prevista: processamento assíncrono
  - registrar como decisão
  - explicar impacto no design

**Relatórios e estatísticas**
- “On-demand” inicialmente
- Evolução possível: pré-cálculo/cache
  - registrar como decisão

**MongoDB**
- Agregados e separação de coleções conforme volume e acesso

**Limites do projeto**
- O projeto não é um LMS completo
- O projeto não é um sistema de provas discursivas com IA
  - apenas correção objetiva e V/F conforme domínio

**Decisões arquiteturais (obrigatório)**
- Incluir **5–10 decisões** no formato:
  - `Decision:`
  - `Context:`
  - `Consequence:`
- As decisões devem orientar outro agente a implementar sem “inventar” arquitetura.

---

## 3) .ai/tech-stack.md — Stack tecnológica (PRESCRITIVO)

Defina explicitamente tecnologias aprovadas.

### Backend

**Java**
- Versão mínima: **TBD**
- Registrar no documento a referência adotada (ex.: LTS)

**Framework**
- Spring Boot (se usado, manter padrão MVC + validação + security)

**Build tool**
- Maven (se não houver decisão explícita, assumir Maven e registrar)

**Dependências aprovadas (se Spring Boot)**
- `spring-boot-starter-web`
- `spring-boot-starter-validation`
- `spring-boot-starter-security` (RBAC/autenticação)
- `spring-boot-starter-data-mongodb`
- `springdoc-openapi-starter-webmvc-ui` (documentação)
- Jackson (via starter)

**Dependências opcionais**
- libs de teste: `spring-boot-starter-test`
- Testcontainers (apenas para testes, se adotado)

**Dependências proibidas (sem justificativa/decisão explícita)**
- frameworks alternativos fora do padrão (ex.: trocar Spring por outro)
- qualquer stack fora do definido (sem ADR)

### Frontend
- React
- TailwindCSS
- Router e libs de forms/validação: somente se justificadas e consistentes

### Banco
- MongoDB (local via Docker/Compose é aceitável)

**Regra geral**
- Qualquer dependência/tecnologia fora desta lista deve ser tratada como **não aprovada**
  até existir decisão no `.ai/architecture.md`.

---

## 4) .ai/business-rules.md — Regras de negócio e domínio

Documente regras claras e operacionais (sem código).

### Domínio e entidades

**Roles**
- `PROFESSOR`
- `ALUNO`

**Entidades do domínio**
- `User`
- `Exam`
- `Question`
- `AnswerKey`
- `Submission`
- `CorrectionResult`
- `ExamReport`
- `ExamStatistics`
- `QuestionIssue`

### Estados e ciclo de vida da Prova (Exam)
- `RASCUNHO` → `PUBLICADA` → `ENCERRADA`

### Regras mínimas
- Somente `PROFESSOR` cria/edita `Exam` e `Question`
- Somente `Exam` `PUBLICADA` aceita `Submission`
- `Exam` `ENCERRADA` não aceita novas submissions
- Datas (`dataInicio`/`dataFim`) se existirem devem ser respeitadas para submissão

### Gabarito (AnswerKey)
- Somente `PROFESSOR` cria/atualiza
- Deve existir para correção automática
- Definir se é pré-requisito para publicar:
  - se não houver decisão explícita: marcar como `TBD` e registrar impacto

### Submissão (Submission)
- Somente `ALUNO` cria submission
- Uma submission pertence a um aluno e uma prova
- Regra de múltiplas tentativas:
  - se não houver decisão explícita: marcar como `TBD` e sugerir padrão “1 tentativa”

### Correção automática
**Tipos suportados**
- `OBJETIVA` (alternativas)
- `VERDADEIRO_FALSO`

**Nota**
- Soma das pontuações das questões corretas
- Questões sem resposta = 0 pontos
- Sem “meio acerto” (a menos que exista decisão explícita)
- `CorrectionResult` deve registrar acertos/erros e detalhes por questão

### Relatórios e Estatísticas

**ExamReport (mínimo)**
- média, maior, menor nota, total de submissões, data de geração

**ExamStatistics (mínimo)**
- percentual de acerto por questão
- distribuição de notas
- lista de “questões com problema”

### Problemas em Questões (QuestionIssue)
**Finalidade**
- marcar questões com comportamento anômalo (ex.: alto índice de erro)

**Heurísticas**
- se não houver limiar definido: registrar como `TBD`

**Operação**
- Deve ser possível registrar manualmente (POST issue)
- Deve ser possível consultar (GET issues)

### Permissões por role (RBAC)

**PROFESSOR**
- CRUD exams, questions, answer-keys
- publicar/encerrar exam
- ver submissions e resultados
- acessar report e statistics
- registrar/consultar issues

**ALUNO**
- listar exams disponíveis
- enviar submissions
- ver resultado da própria submission

---

# Regras finais (obrigatórias)
- Não inclua código.
- Escreva os documentos pensando em outro agente.
- Seja específico e direto.
- Não use frases genéricas.
- Não antecipe implementação (somente regras e decisões).
- Se algo não estiver definido no contexto do projeto:
  - marcar como `TBD`
  - registrar impacto
  - não inventar

---

# Entregável
- Pasta `.ai/` criada
- Quatro arquivos preenchidos conforme especificado
- Ao final, apresentar:
  - lista dos arquivos criados
  - resumo objetivo de cada documento
