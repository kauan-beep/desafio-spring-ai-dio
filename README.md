# Budget AI — API de orçamento com Spring AI

Projeto do desafio **"Desenvolvendo sua API Inteligente com Reconhecimento de Fala e Spring Boot"** (DIO + Itaú).
Baseado no projeto do expert: https://github.com/digitalinnovationone/dio-spring-boot-learning-track (pasta `05-spring-ai`).

## O que o projeto faz

É uma API de orçamento financeiro que entende comandos de voz ou texto:

1. Recebe um arquivo de áudio (ex.: "gastei 50 reais no mercado hoje");
2. Transforma o áudio em texto (Transcription API);
3. A IA entende a intenção (ChatClient);
4. A IA chama uma função real da aplicação (Tool Calling);
5. A transação é salva ou consultada no banco de dados;
6. A resposta final volta em texto ou em áudio (Speech API).

## Melhoria implementada

1. **Validações antes de salvar a transação** (`TransactionService.create`):
   - descrição obrigatória (máx. 120 caracteres);
   - valor maior que zero e no máximo R$ 1.000.000,00;
   - tipo obrigatório (`INCOME` ou `EXPENSE`);
   - data não pode ser futura (se vier vazia, usa hoje);
   - categoria vazia vira "Outros".
2. **Nova consulta financeira**: resumo do mês (receitas, despesas e saldo), disponível em
   `GET /api/transactions/summary` e como **nova ferramenta de Tool Calling** (`getMonthlySummary`).
3. **Testes unitários** das validações (`TransactionServiceTest`).

Quando a validação falha, o erro volta como texto para a IA, que explica o motivo à pessoa usuária.

## Tecnologias

Java 21, Spring Boot 3.4, Spring AI 1.0 (OpenAI), Spring Data JPA, PostgreSQL, Docker Compose, Maven, JUnit 5 e Mockito.

## Como executar

Pré-requisitos: JDK 21, Maven, Docker e uma chave de API da OpenAI.

```bash
# 1. subir o banco de dados
docker compose up -d

# 2. informar a chave da OpenAI
export OPENAI_API_KEY=sua-chave-aqui

# 3. rodar a aplicação
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`.

## Como testar o fluxo principal

Pelo texto (mais fácil para testar a IA):

```bash
curl -X POST http://localhost:8080/api/assistant/text \
  -H "Content-Type: application/json" \
  -d '{"message":"Gastei 50 reais no mercado hoje"}'
```

Pelo áudio (fluxo principal):

```bash
curl -X POST http://localhost:8080/api/assistant/audio -F "file=@comando.mp3"
```

Resposta em áudio (MP3):

```bash
curl -X POST http://localhost:8080/api/assistant/audio/speech -F "file=@comando.mp3" --output resposta.mp3
```

Endpoints REST:

| Método | Rota | O que faz |
|---|---|---|
| POST | `/api/transactions` | Cria uma transação (com validações) |
| GET | `/api/transactions?month=2026-10` | Lista as transações do mês |
| GET | `/api/transactions/summary?month=2026-10` | Resumo do mês |
| DELETE | `/api/transactions/{id}` | Remove uma transação |
| POST | `/api/assistant/text` | Assistente por texto |
| POST | `/api/assistant/audio` | Assistente por áudio (JSON) |
| POST | `/api/assistant/audio/speech` | Assistente por áudio (resposta em MP3) |

Testando a validação (deve retornar erro 400):

```bash
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"mercado","amount":-10,"type":"EXPENSE"}'
```

Rodar os testes unitários: `mvn test`

## Estrutura

```
src/main/java/br/com/budgetai
├── ai          -> BudgetAssistant (ChatClient), BudgetTools (Tool Calling), AudioService
├── controller  -> TransactionController, AssistantController
├── service     -> TransactionService (regras e validações)
├── repository  -> TransactionRepository
├── model       -> Transaction, TransactionType
└── exception   -> InvalidTransactionException, ApiExceptionHandler
```

## O que eu aprendi

Neste desafio eu aprendi como conectar uma Inteligência Artificial a uma aplicação Java com Spring Boot e Spring AI.

- **ChatClient:** é a parte que conversa com o modelo de IA e envia as mensagens da pessoa usuária.
- **Tool Calling:** permite que a IA execute funções reais do código, como salvar uma transação ou calcular o resumo do mês.
- **Transcrição e voz:** entendi o caminho completo do fluxo: áudio vira texto, a IA entende o comando, uma função é executada e a resposta pode voltar em texto ou em áudio.
- **Validações no service:** as regras de negócio (valor maior que zero, data não futura, descrição obrigatória) ficam no código e não dependem da IA. Quando a validação falha, o erro volta para a IA, que explica o motivo à pessoa usuária.
- **Organização em camadas:** aprendi a separar responsabilidades em controller, service, repository, model e ai, o que deixa o projeto mais fácil de entender e evoluir.

**Próximos passos:** adicionar categorias fixas, filtro por categoria e mais testes para as ferramentas de Tool Calling.
## Resultado dos testes

Os testes unitários das validações foram executados com `mvn test` no GitHub Codespaces:

- Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
- BUILD SUCCESS

O fluxo completo com banco de dados (Docker) e chave da OpenAI ainda não foi executado. Fica como próximo passo.
