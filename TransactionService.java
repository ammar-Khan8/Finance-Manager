package com.pfbm.service;

import com.pfbm.dao.TransactionDAO;
import com.pfbm.exception.InvalidAmountException;
import com.pfbm.exception.ValidationException;
import com.pfbm.model.*;
import com.pfbm.util.AppLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {
    private final TransactionDAO txDao;

    public TransactionService(TransactionDAO txDao) {
        this.txDao = txDao;
    }

    /** Parses user text into a positive amount rounded to 2 decimals. */
    public static BigDecimal parseAmount(String text) throws InvalidAmountException {
        BigDecimal value;
        try {
            value = new BigDecimal(text == null ? "" : text.trim());
        } catch (NumberFormatException e) {
            throw new InvalidAmountException("Not a valid amount: '" + text + "'");
        }
        if (value.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    public Transaction add(int userId, boolean income, BigDecimal amount, LocalDate date,
                           Category category, String description) throws InvalidAmountException {
        if (amount == null || amount.signum() <= 0) {
            throw new InvalidAmountException("Amount must be greater than zero");
        }
        if (category == null) {
            throw new ValidationException("Category is required");
        }
        BigDecimal scaled = amount.setScale(2, RoundingMode.HALF_UP);
        LocalDate when = date == null ? LocalDate.now() : date;
        String desc = description == null ? "" : description.trim();
        int id = txDao.nextId();
        Transaction t = income
                ? new Income(id, userId, scaled, when, category, desc)
                : new Expense(id, userId, scaled, when, category, desc);
        txDao.add(t);
        AppLogger.info("Added " + t.getType() + " #" + id + " " + scaled + " " + category + " for user " + userId);
        return t;
    }

    public List<Transaction> list(int userId) {
        return txDao.findByUser(userId).stream()
                .sorted(Comparator.comparing(Transaction::getDate).thenComparingInt(Transaction::getId))
                .collect(Collectors.toList());
    }

    public List<Transaction> listByMonth(int userId, YearMonth month) {
        return list(userId).stream()
                .filter(t -> YearMonth.from(t.getDate()).equals(month))
                .collect(Collectors.toList());
    }

    public boolean delete(int userId, int id) {
        boolean ok = txDao.deleteById(userId, id);
        AppLogger.info((ok ? "Deleted" : "Delete miss for") + " transaction #" + id + " (user " + userId + ")");
        return ok;
    }

    /** All-time income minus expenses. */
    public BigDecimal balance(int userId) {
        BigDecimal total = BigDecimal.ZERO;
        for (Transaction t : txDao.findByUser(userId)) {
            total = t.isIncome() ? total.add(t.getAmount()) : total.subtract(t.getAmount());
        }
        return total;
    }
}
