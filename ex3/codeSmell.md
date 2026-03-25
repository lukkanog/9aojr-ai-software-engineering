## Intervenções Realizadas

### Backend

**Problema:**  
`CorrectionService.correct()` está muito longo e concentra múltiplas responsabilidades, incluindo busca de dados, validação de permissão, cálculo de nota, invalidação de relatório e persistência de entidades no banco.

**Técnica Aplicada:**  
**Extração de Service.**  
Foi criada a classe de domínio `CorrectionCalculator`, responsável por encapsular a lógica de conferência da resposta e cálculo da nota e acertos, separando-a das regras de banco de dados e do controle transacional presentes no `CorrectionService`.

---

**Problema:**  
A classe `ExamService` atua como um **God Object**, gerenciando métodos relativos tanto às propriedades de provas (`Exam`) quanto às propriedades de subdocumentos inerentes (`Question`), o que gera acoplamento excessivo com repositórios e duplicação nas validações.

**Técnica Aplicada:**  
**Separação de Responsabilidades (SRP) via Extração de Service.**  
A lógica de negócio atrelada às entidades `Question` (`addQuestion`, `updateQuestion`, `deleteQuestion`, `getQuestions`) foi movida para um novo `QuestionService`, promovendo melhor reutilização dos métodos `checkOwnership`, `checkStatus` e `checkReadAccess`, que agora estão dispostos publicamente em `ExamService`.  
Além disso, a lógica repetida de validação de `QuestionType.VERDADEIRO_FALSO` foi encapsulada em um método privado dentro de `QuestionService`.

---

### Frontend

**Problema:**  
O componente `ExamDetailPage.jsx` englobava múltiplas responsabilidades, como chamadas à API, listagens visuais para aluno, visões de professor, exibições de correção e controle de envios, ultrapassando 200 linhas. Isso caracteriza um componente muito grande e com layout excessivamente aglutinado.

**Técnica Aplicada:**  
**Extração de Componentes (Componentização).**  
O arquivo principal foi dividido em partes menores:
- `ExamHeader`: controles do professor e sumário da prova;
- `QuestionList`: reutilizado para listagem de questões e formulário;
- `SubmissionsList`: lista de interações utilizadas nas correções.

---

**Problema:**  
Em `ReportPage.jsx` e `QuestionsPage.jsx`, havia chamadas assíncronas que atualizavam o estado (`state updates`) de componentes que poderiam já estar desmontados ao final da promessa, caracterizando risco de **race condition** em `useEffect` e possíveis **memory leaks**.

**Técnica Aplicada:**  
**Variável de Controle com Cleanup ("isMounted Pattern").**  
Foi introduzida uma variável local `isMounted = true`, impedindo mutações de estado caso a função de limpeza (`return () => { isMounted = false; }`) seja executada, mitigando warnings do React e prevenindo atualizações indevidas em componentes desmontados.