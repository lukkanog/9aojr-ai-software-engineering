# Análise de Arquitetura e Boas Práticas

Esta é uma análise da base de código do projeto (`Java 21` + `Spring Boot` + `Mongo` + `React 18` + `Vite`), focada em identificar violações de design, padrões estruturais e vulnerabilidades arquiteturais conforme o stack estabelecido.

Baseado nos módulos encontrados (Controllers, Services, Configurações de Segurança e Componentes React), listamos os principais problemas identificados e suas possíveis resoluções.

---

## 1. Regras de Negócio Fora da Camada Correta

**Problema Encontrado**: Em controllers como `ExamController` e `SubmissionController`, e serviços como `CorrectionService`, o código tenta extrair `user.getId()` ativamente consultando um `UserService.findById(principal.getName())` em cada rota, e então valida manualmente no serviço atributos de posse, como `if (!exam.getProfessorId().equals(userId)) { throw new BusinessRuleException(...); }`. Faltam anotações granulares de autorização.

**Padrão Afetado**: *Separation of Concerns* (Separação de Preocupações) e *Anemic Domain Model*. O Service está infestado de verificações de auditoria e segurança em vez de lógica puramente de domínio (onde um `Exam.isOwner(userId)` traria coesão superior no Domínio Rico).

**Risco Técnico**:
- **Acoplamento Forte**: Serviços precisam lidar com IDs de segurança e persistência que não fariam parte do domínio principal deles.
- **Auditoria Frágil**: Validações manuais iterativas dentro de Services podem ser facilmente esquecidas em novos endpoints e dificultam Testes Unitários eficientes, pois englobam regras além da Correção de um exame em si.

**Organização Proposta**:
- Mover regras de Domínio para dentro das Entidades quando aplicável.
- Implementar *Method Security* (SpEL expressions). Utilizar `@PreAuthorize("@securityExpressions.isExamOwner(authentication, #id)")` diretamente nas rotas da Web API ou repassar o Objeto Principal forte (UserDetails) configurado corretamente no filter sem repetições via DB hit (`findById`).

---

## 2. Uso Inadequado (ou Frágil) do Spring Security

**Problema Encontrado**: O `JwtAuthenticationFilter` extrai o JWT, valida sua validade técnica interna/física (assinatura simétrica do provedor) sem fazer verificação contra blacklists ou deleção/inatividade contra a camada de persistência. Cria um `UsernamePasswordAuthenticationToken` rasamente com ID de usuário e Role. O frontend utiliza uma abordagem de interceptação global rigorosa no Axios que força o refresh por `window.location.href = '/login'`. 

**Padrão Afetado**: *Stateless Security* rígido sem mecanismos de escape / UX em Single Page Applications.

**Risco Técnico**:
- **Backend:** Quando um usuário for bloqueado, trocado de papel de Professor para Aluno, ou perder seus acessos, ele terá acesso persistente total com suas permissões antigas até o JWT daquele contexto expirar. Nenhuma ação de revogação é aplicável na infraestrutura atual.
- **Frontend:** O deslogamento abrupto reseta e destrói todo o estado da GUI (apaga temporários do SPA react Contextos, reducers). O usuário perde eventuais dados transitórios (ex: formulário de edição de questão longo em progresso) durante uma expiração de cookie se um request em background 401 quebrar tudo.

**Organização Proposta**:
- **Tratamento Híbrido**: Verificação de validade de claims de conta das rotas críticas ao menos via cache local (JWT BlockList via Redis Store/DB em casos chaves).
- **UX Frontend**: O Interceptor do Axios deve engatilhar um Evento no React (como disparar um hook global em Contexto) em vez de causar um reload total `window.location`, subindo apenas um React Form Modal de Relogin.

---

## 3. Ausência de Separação entre UI e Serviços / Acesso Direto à API

**Problema Encontrado**: Componentes como `ExamListPage.jsx` gerenciam em blocos monolíticos todo o `useState(loading)`, `useState(error)` e efetuam chamadas diretas como `examsApi.getExams()` no ciclo de vida `useEffect(...)`. Além disso, mesclam o comportamento de dados com as renderizações visuais no JSX.

**Padrão Afetado**:  *Pattern MVC (Arquitetura Componente-Estado-Visão)* e DRY.

**Risco Técnico**:
- **Alto Boilerplate**: Repete lógicas de erro, spinners e `finally(setLoading(false))` pelo sistema inteiro.
- **Sem Cache & Otimizações**: Viola *State Syncing* do Back vs Front (Data Fetching vs UI State), efetuando recarregamentos destrutivos desnecessários ao montar ou remontar componentes de volta à mesma página. Falta testabilidade em lógicas assíncronas isoladas da árvore UI. 

**Organização Proposta**:
- Centralizar em **Custom Hooks** que contêm os serviços `useExams()` removendo todo o *fetching process* do arquivo Visual `.jsx`.
- Integrar bibliotecas especializadas de Estado de Servidor, primariamente preferindo **React Query (TanStack)** ou swr. 

---

## 4. Proteção de Rotas Inconsistente

**Problema Encontrado**: Muitos Endpoints do Backend (ex: listagem GET `findByExam` do `SubmissionController`) omitem o decorador `@PreAuthorize`, deixando as barreiras de checagem para delegar a validação aos retornos dos Services de forma manual dentro do Java. No Front (`ProtectedRoute.jsx`), ele só confere validade simples de arranjos em Auth Roles (`includes(user?.role)`), apenas barrando com replaces do Router DOM, mas com tratamento de papéis fraco se existirem rotas profundas.

**Padrão Afetado**: *Defense in Depth* (Segurança em Profundidade) e Authorization-first APIs.

**Risco Técnico**: 
- Endpoint exposto caso programadores ignorem ou engavetem implementações de RBAC por erro nos sub-serviços. Violações clássicas podem gerar falhas tipo **IDOR - Insecure Direct Object Reference** caso as permissões do usuário em um método interno retornem True erroneamente por esquecimento da re-verificação da lógica no repositório.

**Organização Proposta**:
- Evitar ao máximo delegar permissão de escopo nos Services de backend, mas travar rigidamente (AOP Patterns) ao nível declarativo via Spring Security Method Authorization nos Controllers. 

---

## 5. Acoplamento Excessivo entre Frontend e Backend

**Problema Encontrado**: O Frontend em React confia acoplado a Strings Mágicas estáticas originárias do Model Backend em quase todas as telas. Por exemplo, em `ExamListPage.jsx`, há objetos e chaves configurados em volta de strings atreladas à API JSON: `STATUS_COLORS = { 'RASCUNHO': ..., 'PUBLICADA': ..., 'ENCERRADA': ... }`. 

**Padrão Afetado**: *Tight Coupling* (Acoplamento Forte) / Anti-Corruption Layers.

**Risco Técnico**: 
- Resiliência Baixa perante Evoluções da API. Se uma refatoração minúscula em qualquer enum do Spring Boot for feita ("ENCERRADA" para "FINALIZADA" ou traduzido "CLOSED" etc.), o frontend inteiro quebra sem reportar erros ao compilador do Vite, gerando falhas visuais silenciosas e de regras nos status dos exames para o cliente.

**Organização Proposta**:
- Extrair *Magic Strings* e constantes cruas em dicionários universais localizados nos `utils` ou `constants` do Typescript/JS do React (`enums/ExamStatus.js`), provendo mapping resiliente entre DTO Payload do MongoDB e os Modelos de Visualização Interna do Javascript; criando assim Adaptadores (Serializadores) claros.
