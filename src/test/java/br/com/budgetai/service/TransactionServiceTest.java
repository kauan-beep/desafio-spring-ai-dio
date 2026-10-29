package br.com.budgetai.service;

import br.com.budgetai.exception.InvalidTransactionException;
import br.com.budgetai.model.Transaction;
import br.com.budgetai.model.TransactionType;
import br.com.budgetai.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Test
    void shouldRejectZeroOrNegativeAmount() {
        TransactionService service = new TransactionService(repository);

        assertThrows(InvalidTransactionException.class, () ->
                service.create("mercado", BigDecimal.ZERO, TransactionType.EXPENSE, null, null));
        assertThrows(InvalidTransactionException.class, () ->
                service.create("mercado", new BigDecimal("-5"), TransactionType.EXPENSE, null, null));

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectBlankDescription() {
        TransactionService service = new TransactionService(repository);

        assertThrows(InvalidTransactionException.class, () ->
                service.create("  ", new BigDecimal("10"), TransactionType.EXPENSE, null, null));
    }

    @Test
    void shouldRejectFutureDate() {
        TransactionService service = new TransactionService(repository);

        assertThrows(InvalidTransactionException.class, () ->
                service.create("curso", new BigDecimal("10"), TransactionType.EXPENSE,
                        null, LocalDate.now().plusDays(1)));
    }

    @Test
    void shouldSaveValidTransactionWithDefaultCategoryAndDate() {
        TransactionService service = new TransactionService(repository);
        when(repository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

        Transaction saved = service.create("  mercado ", new BigDecimal("50.5"),
                TransactionType.EXPENSE, null, null);

        assertEquals("mercado", saved.getDescription());
        assertEquals(new BigDecimal("50.50"), saved.getAmount());
        assertEquals("Outros", saved.getCategory());
        assertEquals(LocalDate.now(), saved.getDate());
    }
                               }
