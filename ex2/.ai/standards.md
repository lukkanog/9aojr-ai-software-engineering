# Padrões de Código e Estilo

---

## Backend — Java 21 + Maven + Spring Boot 3.x

### Linguagem e Versão

- **Java 21** é a versão obrigatória do projeto.
- Recursos modernos (records, sealed classes, pattern matching) devem ser usados **com parcimônia** e apenas quando agregarem clareza.
- Records são obrigatórios para DTOs de request e response, exceto quando herança ou mutabilidade forem necessárias.
- `Optional` é permitido **exclusivamente** em tipos de retorno de métodos. Nunca usar `Optional` como campo de classe, parâmetro de método ou elemento de coleção.
- Stream API deve ser usada apenas para transformações simples e de leitura linear. Streams aninhados, com mais de 3 operações encadeadas ou com side-effects são proibidos.

### Build

- **Maven** é o sistema de build. Não introduzir Gradle.
- Dependências devem ser declaradas com versão gerenciada pelo BOM do Spring Boot sempre que possível.

### Organização de Pacotes

Estrutura obrigatória dentro do pacote raiz do projeto:

```
com.projeto.examcorrection
├── controller/    # Endpoints REST (apenas roteamento, validação de entrada e delegação)
├── service/       # Regras de negócio (toda lógica fica aqui)
├── repository/    # Acesso a dados (interfaces do Spring Data MongoDB)
├── domain/        # Entidades/documentos do MongoDB
├── dto/           # Request e Response DTOs (preferencialmente records)
├── config/        # Configurações do Spring e beans de infraestrutura
├── security/      # Configuração de autenticação, autorização e filtros de segurança
└── error/         # Classes de erro padronizado, exception handlers e exceções customizadas
```

### Camadas e Responsabilidades

| Camada       | Responsabilidade                                                             | Proibido fazer                                              |
|--------------|------------------------------------------------------------------------------|-------------------------------------------------------------|
| Controller   | Receber request, validar entrada (`@Valid`), delegar ao Service, retornar response | Conter lógica de negócio, acessar Repository diretamente    |
| Service      | Orquestrar regras de negócio, transformar dados, coordenar repositórios      | Receber `HttpServletRequest`, retornar `ResponseEntity`     |
| Repository   | Persistência e consultas ao MongoDB                                          | Conter lógica de negócio                                    |
| Domain       | Representar documentos/entidades do MongoDB                                  | Conter lógica de apresentação ou validação de request       |
| DTO          | Transportar dados entre Controller e Service/Client                          | Conter lógica de negócio ou referências a domain diretamente|

### Convenções REST

- Rotas usam **substantivos no plural** e **lowercase com hífens** quando necessário: `/exams`, `/submissions`, `/answer-key`.
- Verbos HTTP conforme semântica REST padrão:
  - `GET` para leitura
  - `POST` para criação e ações (publish, close, correct)
  - `PUT` para atualização completa
  - `DELETE` para remoção
- Ações que não são CRUD (publicar, encerrar, corrigir) usam `POST` em sub-recurso: `POST /exams/{id}/publish`.

### Status Codes Padronizados

| Código | Uso                                                        |
|--------|------------------------------------------------------------|
| 200    | Sucesso em leitura ou atualização                          |
| 201    | Recurso criado com sucesso                                 |
| 204    | Operação sem conteúdo de retorno (ex.: DELETE)             |
| 400    | Request malformado ou dados inválidos                      |
| 401    | Não autenticado                                            |
| 403    | Autenticado mas sem permissão (role insuficiente)          |
| 404    | Recurso não encontrado                                     |
| 409    | Conflito (ex.: submissão duplicada)                        |
| 422    | Violação de regra de negócio (dados válidos sintaticamente)|
| 500    | Erro interno inesperado                                    |

### Validação

- Toda validação de entrada usa `jakarta.validation` (`@NotBlank`, `@NotNull`, `@Size`, `@Valid` etc.).
- Controllers devem anotar parâmetros de request body com `@Valid`.
- Validações de negócio (ex.: "exam deve estar publicada") ficam obrigatoriamente no Service.

### Resposta de Erro Padronizada

Toda resposta de erro deve seguir este formato JSON:

```json
{
  "code": "EXAM_NOT_FOUND",
  "message": "Prova não encontrada com o ID informado.",
  "details": null,
  "traceId": "abc123-def456"
}
```

- `code`: string constante em UPPER_SNAKE_CASE identificando o tipo de erro.
- `message`: mensagem legível para o consumidor da API.
- `details`: objeto opcional com detalhes adicionais (ex.: erros de validação por campo).
- `traceId`: identificador de correlação da requisição.

Um `@RestControllerAdvice` global deve capturar exceções e converter para este formato.

### Logs

- Usar SLF4J como fachada de log.
- Todo log de requisição deve incluir `traceId` / `correlationId`.
- **Proibido** logar: senhas, tokens, dados pessoais sensíveis (PII) desnecessários.
- Níveis de log:
  - `ERROR`: falhas inesperadas que impedem a operação.
  - `WARN`: situações anômalas recuperáveis.
  - `INFO`: operações de negócio relevantes (criação de prova, submissão, correção).
  - `DEBUG`: detalhes técnicos para troubleshooting (desabilitado em produção).

### Testes

- **Obrigatório**: testes unitários para:
  - Lógica de correção automática (cálculo de nota, acertos, erros).
  - Regras de transição de status da prova (RASCUNHO → PUBLICADA → ENCERRADA).
  - Validações de regras de negócio no Service.
- Testes unitários usam JUnit 5 + Mockito.
- **Opcional**: testes de integração com MongoDB via Testcontainers.
- Nomenclatura de testes: `deve_<resultado>_quando_<condição>` ou equivalente descritivo.

---

## Frontend — React + TailwindCSS

### Componentização

- Componentes devem ser **pequenos**, com responsabilidade única.
- Nome de componente: `PascalCase` (ex.: `ExamList`, `SubmissionForm`, `QuestionCard`).
- Separar **pages** (views/telas) de **components** (elementos reutilizáveis):

```
src/
├── pages/         # Telas/views (ExamListPage, ExamDetailPage, LoginPage)
├── components/    # Componentes reutilizáveis (Button, Modal, QuestionCard)
├── api/           # Camada de comunicação com a API REST
├── hooks/         # Custom hooks
├── utils/         # Funções utilitárias puras
├── contexts/      # Context API (apenas se necessário)
└── styles/        # Arquivos CSS globais e configuração do Tailwind
```

### Estado

- Preferir **estado local** (`useState`, `useReducer`) sempre que possível.
- Context API apenas para dados verdadeiramente globais (ex.: usuário autenticado).
- Não introduzir Redux, Zustand ou qualquer gerenciador de estado global sem decisão registrada em `architecture.md`.

### Consumo de API

- Toda comunicação com o backend deve passar pela camada `api/`.
- Cada recurso da API deve ter seu próprio módulo (ex.: `api/exams.js`, `api/submissions.js`).
- Tratamento de erro padronizado:
  - Exibir mensagens amigáveis ao usuário (nunca stack traces ou códigos técnicos).
  - Erros de rede devem exibir mensagem genérica de indisponibilidade.
  - Erros 401 devem redirecionar para login.
  - Erros 403 devem exibir mensagem de permissão insuficiente.

### Acessibilidade e Responsividade

- **Responsividade obrigatória**: toda interface deve funcionar em desktop e mobile.
- Todo `<input>` deve ter um `<label>` associado (via `htmlFor` ou aninhamento).
- Foco visível obrigatório: nunca remover `outline` de elementos focáveis sem substituto adequado.
- Navegação por teclado deve funcionar para todos os fluxos principais.
- Usar atributos `aria-*` quando a semântica HTML nativa não for suficiente.

### Estilo com TailwindCSS

- Usar classes utilitárias do Tailwind diretamente nos componentes.
- Para padrões repetitivos, extrair para classes com `@apply` no CSS ou criar componentes reutilizáveis.
- Não misturar CSS-in-JS com Tailwind.
- Breakpoints padrão do Tailwind para responsividade: `sm`, `md`, `lg`, `xl`.

### Nomenclatura

| Elemento            | Convenção         | Exemplo                        |
|---------------------|-------------------|--------------------------------|
| Componente          | PascalCase        | `ExamCard`, `SubmissionForm`   |
| Arquivo componente  | PascalCase.jsx    | `ExamCard.jsx`                 |
| Página              | PascalCase + Page | `ExamListPage.jsx`             |
| Hook customizado    | useCamelCase      | `useExams`, `useAuth`          |
| Módulo API          | camelCase.js      | `exams.js`, `submissions.js`   |
| Variáveis/funções   | camelCase         | `examList`, `handleSubmit`     |
| Constantes          | UPPER_SNAKE_CASE  | `API_BASE_URL`, `MAX_RETRIES`  |
