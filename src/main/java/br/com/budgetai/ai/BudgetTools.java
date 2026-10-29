package br.com.budgetai.ai;

import br.com.budgetai.exception.InvalidTransactionException;
import br.com.budgetai.model.Transaction;
import br.com.budgetai.model.TransactionType;
import br.com.budgetai.service.TransactionService;
import br.com.budgetai.service.TransactionService.MonthlySummary;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Funções reais da aplicação que a IA pode chamar (Tool Calling).
 * As mensagens de erro são devolvidas como texto para a IA explicar à pessoa usuária.
 */
@Component
public class BudgetTools {

    private final TransactionService service;

    public BudgetTools(TransactionService service) {
        this.service = service;
    }

    @Tool(description = "Registra uma nova transação financeira: receita (INCOME) ou despesa (EXPENSE).")
    public String createTransaction(
            @ToolParam(description = "Descrição curta, por exemplo: mercado, salário, uber") String description,
            @ToolParam(description = "Valor em reais, sempre positivo") double amount,
            @ToolParam(description = "INCOME para receita ou EXPENSE para despesa") String type,
            @ToolParam(description = "Categoria, por exemplo: Alimentação, Transporte", required = false) String category,
            @ToolParam(description = "Data no formato yyyy-MM-dd. Omitir para usar hoje", required = false) String date) {

        try {
            TransactionType transactionType = TransactionType.valueOf(type.trim().toUpperCase());
            LocalDate parsedDate = (date == null || date.isBlank()) ? null : LocalDate.parse(date.trim());

            Transaction saved = service.create(
                    description, BigDecimal.valueOf(amount), transactionType, category, parsedDate);

            return "Transação salva com sucesso: " + format(saved);
        } catch (InvalidTransactionException e) {
            return "Não foi possível salvar: " + e.getMessage();
        } catch (IllegalArgumentException | NullPointerException | DateTimeParseException e) {
            return "Não foi possível salvar: tipo ou data inválidos. Use INCOME/EXPENSE e data yyyy-MM-dd.";
        }
    }

    @Tool(description = "Lista as transações de um mês. Se o mês for omitido, usa o mês atual.")
    public String listTransactions(
            @ToolParam(description = "Mês no formato yyyy-MM, por exemplo 2026-10", required = false) String month) {

        try {
            List<Transaction> transactions = service.listByMonth(parseMonth(month));
            if (transactions.isEmpty()) {
                return "Nenhuma transação encontrada nesse mês.";
            }
            StringBuilder sb = new StringBuilder();
            transactions.forEach(t -> sb.append(format(t)).append("\n"));
            return sb.toString();
        } catch (DateTimeParseException e) {
            return "Mês inválido. Use o formato yyyy-MM.";
        }
    }

    // NOVA FERRAMENTA: resumo mensal (receitas, despesas e saldo)
    @Tool(description = "Calcula o total de receitas, o total de despesas e o saldo de um mês. Se o mês for omitido, usa o mês atual.")
    public String getMonthlySummary(
            @ToolParam(description = "Mês no formato yyyy-MM, por exemplo 2026-10", required = false) String month) {

        try {
            MonthlySummary s = service.summary(parseMonth(month));
            return "Mês " + s.month()
                    + " | Receitas: R$ " + s.totalIncome()
                    + " | Despesas: R$ " + s.totalExpense()
                    + " | Saldo: R$ " + s.balance();
        } catch (DateTimeParseException e) {
            return "Mês inválido. Use o formato yyyy-MM.";
        }
    }

    private YearMonth parseMonth(String month) {
        return (month == null || month.isBlank()) ? YearMonth.now() : YearMonth.parse(month.trim());
    }

    private String format(Transaction t) {
        return t.getDate() + " | " + t.getType() + " | R$ " + t.getAmount()
                + " | " + t.getDescription() + " (" + t.getCategory() + ")";
    }
}
