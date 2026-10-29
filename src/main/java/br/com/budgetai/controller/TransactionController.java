package br.com.budgetai.controller;

import br.com.budgetai.model.Transaction;
import br.com.budgetai.model.TransactionType;
import br.com.budgetai.service.TransactionService;
import br.com.budgetai.service.TransactionService.MonthlySummary;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    public record TransactionRequest(String description, BigDecimal amount,
                                     TransactionType type, String category, LocalDate date) {
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Transaction create(@RequestBody TransactionRequest request) {
        return service.create(request.description(), request.amount(),
                request.type(), request.category(), request.date());
    }

    @GetMapping
    public List<Transaction> list(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.listByMonth(month == null ? YearMonth.now() : month);
    }

    @GetMapping("/summary")
    public MonthlySummary summary(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return service.summary(month == null ? YearMonth.now() : month);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
