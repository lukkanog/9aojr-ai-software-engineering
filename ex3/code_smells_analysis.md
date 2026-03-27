# Análise de Débitos Técnicos e Code Smells

Esta é uma análise da base de código do projeto em busca de padrões obsoletos, código verboso e desatualizado em relação a todo potencial da stack atual (`Java 21`, `Spring Boot 3.4.x`, `Mongo 7`, `React 18+`, `React Router 7` e `TailwindCSS 4`).

Listamos abaixo os principais cenários identificados pelo mapeamento investigativo e suas respectivas modernizações.

---

## 1. Verbosidade Extrema com Value Objects (Snapshot Models)

**Problema Encontrado**: Múltiplas classes de dados de domínio puramente estruturais, como a classe `QuestionDetail.java` em `com.projeto.examcorrection.domain`, são configuradas com construtores vazios, encapsulamento clichê (dezenas de Getters/Setters) e nenhum método de negócio.

**Padrão Afetado**: *Boilerplate Excessivo* vs Imutabilidade.

**Refatoração Sugerida**:
A classe possui 54 linhas que podem ser substituídas por uma construção nativa do Java 14+:
```java
public record QuestionDetail(
    String questionId, 
    boolean correta, 
    String respostaAluno, 
    String respostaEsperada, 
    double pontuacaoObtida
) {}
```
**Ganho Esperado**:
- **Concisão e Legibilidade**: Redução de ~50 linhas e clareza imediata sobre os parâmetros do objeto.
- **Segurança**: Objetos resultantes das correções (e do MongoDB via Spring Data) nascem imutáveis, thread-safes, e evitam side-effects em run-time.

---

## 2. Programação Imperativa e Loops Manuais (Falta de Streams/Functional)

**Problema Encontrado**: O componente `CorrectionCalculator.java` gerencia mutabilidade de alto nível de maneira puramente imperativa. Ele declara dezenas de ponteiros no topo (`int acertos = 0; int erros = 0; double notaFinal...`) e realiza um enorme loop `for (Question q : questions)` com mais de 25 linhas efetuando lógicas intrincadas de condição e incrementações (`acertos++`, `detalhes.add(...)`).

**Padrão Afetado**: *Imperative Aggregation* e *Mutable Global-Scope Variables* locais de processamento.

**Refatoração Sugerida**:
Tirar proveito da arquitetura de `Java Streams` (`.stream()`) e lambdas do Java 21 em conjunto com instâncias locais. 
Podemos mapear a lista list de `Question` para `QuestionDetail` gerando Stream Results e delegar a contagem sumária para Operadores de Redução e Records locais.

```java
var detalhes = questions.stream()
    .map(q -> new QuestionDetail(... correta, pontos ...))
    .toList();

int acertos = (int) detalhes.stream().filter(QuestionDetail::correta).count();
double notaFinal = detalhes.stream().mapToDouble(QuestionDetail::pontuacaoObtida).sum();
```

**Ganho Esperado**:
- Facilita testabilidade isolada por meio de métodos encadeados ou paralelização em massa (`.parallelStream()`).
- Omitir side effects acidentais em contadores complexos que causam "Bug de Incrementação Off-by-one".

---

## 3. Padrões Antigos de React Data Fetching (Fetch-on-Render Waterfall)

**Problema Encontrado**: Arquivos JSX densos como o `CorrectionResultPage.jsx` ainda utilizam a abordagem manual e obsoleta do React antigo para efetuar buscas assíncronas: instanciamento sequencial em massa de `useState(null)`, `useState(error)` e execução atada no ciclo de vida através da casca pura do `useEffect(() => { api.get... }, [])`. 

**Padrão Afetado**: *Imperative Data Fetching* e violação de *State Management Separation*.

**Refatoração Sugerida**:
Eliminar todo o bloco assíncrono visual em prol do ecossistema do React Query (reaproveitando o stack `@tanstack/react-query` inserido globalmente nas outras refatorações) ou da API de `loader` do React Router 6.4+.

```jsx
// Substituindo as dezenas de linhas da gerência manual assíncrona para:
const { data: result, isLoading: !result, error } = useCorrectionResult(submissionId);
```
**Ganho Esperado**:
- Abandono de Loading Spinners arbitrários quebrados (evita concorrências visuais / Race Conditions).
- Obtenção automática de **Caching Inteligente**, requisições duplicadas sendo abstraídas em background *(Stale-while-revalidate)*, retry em falha de rede e redução massiva de Hooks repetitivos nos componentes finais.

---

## 4. Junções Lógicas Acopladas em Memória vs DB Aggregation (MongoDB 7)

**Problema Encontrado**: Dentro do `SubmissionService.findByExamId`, há uma lógica de resolução de nomes "em batch" (N+1 Solution manual) utilizando Java Collection Mappings. O código varre a lista de submissões retornada do banco, extrai IDs, bate no `userRepository.findAllById(alunoIds)` separadamente para enfim aplicar `.stream().collect(Collectors.toMap(...))` manipulando listas dentro da JVM para emular um JOIN relacional.

**Padrão Afetado**: *Application-Side Joins* e subutilização da camada de Banco de Dados de Documentos.

**Refatoração Sugerida**:
Graças ao Spring Data e o motor do MongoDB 7, pode-se realizar o acoplamento de subdocumentos e arrays no próprio Database via **Aggregation Pipelines** ou anotações `@DocumentReference`. Pela aggregation framework de `$lookup` no Spring (`AggregationOperations`), o Java já recebe o objeto DTO preenchido pelo Mongo (ex: `SubmissionWithStudentName`) com os relatórios prontos.

**Ganho Esperado**:
- Tráfego otimizado na rede interna entre a API Java e o DataNode MongoDB, mitigando Payloads pesados com arrays indesejados (`findAllById`). Alivia a CPU da API por não forçar alocação de HashMaps em processamento para cruzar dois Models distintos apenas para renderizar uma Visualização/Response.
