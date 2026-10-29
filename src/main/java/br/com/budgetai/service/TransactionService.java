package br.com.budgetai.service;

import br.com.budgetai.exception.InvalidTransactionException;
import br.com.budgetai.model.Transaction;
import br.com.budgetai.model.TransactionType;
import br.com.budgetai.repository.TransactionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class TransactionService {

    private static final BigDecimal MAX_AMOUNT = new BigDecimal("1000000");

    private final TransactionRepository repository;

    public TransactionService(TransactionRepository repository) {
        this.repository = repository;
    }

    /** Resumo de um mês: receitas, despesas e saldo. */
    public record MonthlySummary(String month, BigDecimal totalIncome,
                                 BigDecimal totalExpense, BigDecimal balance) {
    }

    // MELHORIA DO DESAFIO: validações antes de salvar a transação
    public Transaction create(String description, BigDecimal amount, TransactionType type,
                              String category, LocalDate date) {

        if (description == null || description.isBlank()) {
            throw new InvalidTransactionException("A descrição é obrigatória.");
        }
        if (description.trim().length() > 120) {
            throw new InvalidTransactionException("A descrição deve ter no máximo 120 caracteres.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("O valor deve ser maior que zero.");
        }
        if (amount.compareTo(MAX_AMOUNT) > 0) {
            throw new InvalidTransactionException("O valor máximo por transação é R$ 1.000.000,00.");
        }
        if (type == null) {
            throw new InvalidTransactionException("O tipo deve ser INCOME (receita) ou EXPENSE (despesa).");
        }

        LocalDate finalDate = (date == null) ? LocalDate.now() : date;
        if (finalDate.isAfter(LocalDate.now())) {
            throw new InvalidTransactionException("A data não pode ser futura.");
        }

        String finalCategory = (category == null || category.isBlank()) ? "Outros" : category.trim();

        Transaction transaction = new Transaction(
                description.trim(),
                amount.setScale(2, RoundingMode.HALF_UP),
                type,
                finalCategory,
                finalDate);

        return repository.save(transaction);
    }

    public List<Transaction> listByMonth(YearMonth month) {
        return repository.findByDateBetweenOrderByDateDesc(month.atDay(1), month.atEndOfMonth());
    }

    // NOVA CONSULTA FINANCEIRA: resumo mensal
    public MonthlySummary summary(YearMonth month) {
        List<Transaction> transactions = listByMonth(month);

        BigDecimal income = sumByType(transactions, TransactionType.INCOME);
        BigDecimal expense = sumByType(transactions, TransactionType.EXPENSE);

        return new MonthlySummary(month.toString(), income, expense, income.subtract(expense));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transação não encontrada: " + id);
        }
        repository.deleteById(id);
    }

    private BigDecimal sumByType(List<Transaction> transactions, TransactionType type) {
        return transactions.stream()
                .filter(t -> t.getType() == type)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
          }
