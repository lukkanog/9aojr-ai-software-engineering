# Stack Tecnológica

> Qualquer tecnologia fora desta lista é considerada **não aprovada** e requer decisão registrada em `architecture.md` antes de ser adotada.

---

## Backend

| Tecnologia                  | Versão / Especificação       | Finalidade                                      |
|-----------------------------|------------------------------|--------------------------------------------------|
| **Java**                    | 21 (LTS)                    | Linguagem principal do backend                   |
| **Maven**                   | 3.9+                        | Build e gerenciamento de dependências            |
| **Spring Boot**             | 3.x (compatível com Java 21)| Framework principal (REST, DI, configuração)     |
| **Spring Web**              | via Spring Boot starter      | Endpoints REST                                   |
| **Spring Data MongoDB**     | via Spring Boot starter      | Acesso e persistência no MongoDB                 |
| **Spring Security**         | via Spring Boot starter      | Autenticação e autorização (RBAC)                |
| **JWT (jjwt ou equivalente)** | Compatível com Spring Security | Geração e validação de tokens JWT              |
| **Jakarta Validation**      | via Spring Boot starter      | Validação de entrada (`@Valid`, `@NotBlank` etc.) |
| **SpringDoc OpenAPI**       | 2.x                         | Documentação automática da API (Swagger UI)      |
| **SLF4J + Logback**         | via Spring Boot (padrão)     | Logging estruturado                              |
| **JUnit 5**                 | via Spring Boot starter test | Testes unitários                                 |
| **Mockito**                 | via Spring Boot starter test | Mocking em testes unitários                      |


---

## Frontend

| Tecnologia         | Versão / Especificação | Finalidade                                    |
|--------------------|------------------------|-----------------------------------------------|
| **React**          | 18.x+                 | Biblioteca de UI principal                    |
| **React Router**   | 6.x+                  | Roteamento client-side (SPA)                  |
| **TailwindCSS**    | 3.x+                  | Framework CSS utilitário                      |
| **Vite**           | 5.x+                  | Bundler e dev server                          |
| **Axios ou Fetch** | Nativa ou última estável | Comunicação HTTP com a API                  |

---

## Banco de Dados

| Tecnologia   | Versão / Especificação | Finalidade                       |
|--------------|------------------------|----------------------------------|
| **MongoDB**  | 7.x+ (ou compatível)  | Banco de dados principal         |

---

## Infraestrutura e Ferramentas de Desenvolvimento

| Ferramenta       | Finalidade                                             |
|------------------|--------------------------------------------------------|
| **Git**          | Controle de versão                                     |
| **Docker**       | Containerização do MongoDB para desenvolvimento local |
| **npm**          | Gerenciamento de pacotes do frontend                   |

---

## Regras de Adoção

1. **Novas dependências backend**: devem ser compatíveis com Spring Boot 3.x e Java 21. Usar starters do Spring Boot como primeira opção.
2. **Novas dependências frontend**: devem ser compatíveis com React 18+ e Vite. Justificar necessidade antes de adicionar.
3. **Gerenciadores de estado global** (Redux, Zustand, MobX): não aprovados. Usar Context API + hooks apenas se necessário.
4. **CSS-in-JS** (styled-components, Emotion): não aprovado. Usar TailwindCSS.
5. **ORMs alternativos**: não aprovado. Usar Spring Data MongoDB exclusivamente.
6. **Qualquer tecnologia não listada**: exige decisão documentada em `architecture.md` com formato ADR antes de ser adotada.
