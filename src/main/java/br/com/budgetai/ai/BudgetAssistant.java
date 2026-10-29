package br.com.budgetai.ai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BudgetAssistant {

    private static final String SYSTEM_PROMPT = """
            Você é um assistente de orçamento pessoal. Responda sempre em português do Brasil,
            de forma curta e clara. Use as ferramentas disponíveis para registrar transações,
            listar transações e calcular o resumo do mês. Nunca invente valores: se faltar
            informação essencial (como o valor), peça à pessoa usuária. Se uma ferramenta
            retornar erro, explique o motivo de forma simples.
            """;

    private final ChatClient chatClient;

    public BudgetAssistant(ChatClient.Builder builder, BudgetTools tools) {
        this.chatClient = builder
                .defaultTools(tools)
                .build();
    }

    public String ask(String message) {
        return chatClient.prompt()
                .system(SYSTEM_PROMPT + " A data de hoje é " + LocalDate.now() + ".")
                .user(message)
                .call()
                .content();
    }
}
