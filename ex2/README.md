# Sistema de Correção Automática de Provas

## Visão Geral
Sistema web para criação, gerenciamento e correção automática de provas com dois papéis: **PROFESSOR** e **ALUNO**.

## Stack Tecnológica
- **Backend**: Java 21, Spring Boot 3.4.1, Spring Security (JWT), Spring Data MongoDB
- **Frontend**: React 18, Vite, TailwindCSS 4, React Router, Axios
- **Banco**: MongoDB 7

## Pré-requisitos
- Java 21
- Maven
- Node.js 18+
- Docker (para MongoDB)

## Como rodar

### 1. Banco de Dados
```bash
docker-compose up -d
```

### 2. Backend
```bash
cd backend
mvn spring-boot:run
```
Backend roda em `http://localhost:8080`

### 3. Frontend
```bash
cd frontend
npm install
npm run dev
```
Frontend roda em `http://localhost:5173`

## Endpoints da API

### Autenticação
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| POST | `/auth/register` | Cadastro | Público |
| POST | `/auth/login` | Login | Público |
| GET | `/auth/me` | Dados do usuário autenticado | Autenticado |
| POST | `/auth/logout` | Logout | Autenticado |

### Usuários
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/users` | Listar usuários | PROFESSOR |
| GET | `/users/{id}` | Buscar usuário | PROFESSOR |
| POST | `/users` | Criar usuário | PROFESSOR |
| PUT | `/users/{id}` | Atualizar usuário | PROFESSOR |
| DELETE | `/users/{id}` | Remover usuário | PROFESSOR |

### Provas
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/exams` | Listar provas | Autenticado |
| GET | `/exams/{id}` | Buscar prova | Autenticado |
| POST | `/exams` | Criar prova | PROFESSOR |
| PUT | `/exams/{id}` | Atualizar prova | PROFESSOR |
| DELETE | `/exams/{id}` | Remover prova | PROFESSOR |
| POST | `/exams/{id}/publish` | Publicar prova | PROFESSOR |
| POST | `/exams/{id}/close` | Encerrar prova | PROFESSOR |

### Questões
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/exams/{examId}/questions` | Listar questões | Autenticado |
| POST | `/exams/{examId}/questions` | Adicionar questão | PROFESSOR |
| PUT | `/questions/{id}` | Atualizar questão | PROFESSOR |
| DELETE | `/questions/{id}` | Remover questão | PROFESSOR |

### Gabarito
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/exams/{examId}/answer-key` | Buscar gabarito | PROFESSOR |
| POST | `/exams/{examId}/answer-key` | Criar gabarito | PROFESSOR |
| PUT | `/exams/{examId}/answer-key` | Atualizar gabarito | PROFESSOR |

### Submissões
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/exams/{examId}/submissions` | Listar submissões | Autenticado |
| GET | `/submissions/{id}` | Buscar submissão | Autenticado |
| POST | `/exams/{examId}/submissions` | Submeter respostas | ALUNO |

### Correção
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| POST | `/submissions/{id}/correct` | Corrigir submissão | PROFESSOR |
| GET | `/submissions/{id}/correction-result` | Resultado da correção | Autenticado |

### Relatórios e Estatísticas
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/exams/{examId}/report` | Relatório da prova | PROFESSOR |
| GET | `/exams/{examId}/statistics` | Estatísticas da prova | PROFESSOR |

### Issues de Questão
| Método | Endpoint | Descrição | Acesso |
|--------|----------|-----------|--------|
| GET | `/questions/{questionId}/issues` | Listar issues | PROFESSOR |
| POST | `/questions/{questionId}/issues` | Registrar issue | PROFESSOR |

## Regras de Negócio
- Provas seguem o ciclo: **RASCUNHO → PUBLICADA → ENCERRADA**
- Gabarito é imutável após existirem submissões
- Aluno pode submeter apenas 1 vez por prova
- Submissão só é permitida dentro da janela de datas
- Correção automática compara respostas com gabarito
- Issues são geradas automaticamente com base em thresholds configuráveis
