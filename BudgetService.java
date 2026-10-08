package com.pfbm.service;

import com.pfbm.dao.BudgetDAO;
import com.pfbm.dao.TransactionDAO;
import com.pfbm.exception.ValidationException;
import com.pfbm.model.*;
import com.pfbm.util.AppLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class BudgetService {
    private final BudgetDAO budgetDao;
    private final TransactionDAO txDao;

    public BudgetService(BudgetDAO budgetDao, TransactionDAO txDao) {
        this.budgetDao = budgetDao;
        this.txDao = txDao;
    }

    public Budget setBudget(int userId, Category category, YearMonth month, BigDecimal limit) {
        if (category == null) {
            throw new ValidationException("Category is required");
        }
        if (month == null) {
            throw new ValidationException("Month is required");
        }
        if (limit == null || limit.signum() <= 0) {
            throw new ValidationException("Budget limit must be greater than zero");
        }
        Budget b = new Budget(userId, category, month, limit.setScale(2, RoundingMode.HALF_UP));
        budgetDao.upsert(b);
        AppLogger.info("Budget set: user " + userId + " " + category + " " + month + " = " + b.getLimit());
        return b;
    }

    /** Budgets for the month, each paired with that month's expense total for the category. */
    public List<BudgetStatus> statusFor(int userId, YearMonth month) {
        Map<Category, BigDecimal> spent = new EnumMap<>(Category.class);
        for (Transaction t : txDao.findByUser(userId)) {
            if (!t.isIncome() && YearMonth.from(t.getDate()).equals(month)) {
                spent.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
        List<BudgetStatus> out = new ArrayList<>();
        for (Budget b : budgetDao.findByUser(userId)) {
            if (b.getMonth().equals(month)) {
                out.add(new BudgetStatus(b.getCategory(), b.getLimit(),
                        spent.getOrDefault(b.getCategory(), BigDecimal.ZERO)));
            }
        }
        out.sort(Comparator.comparing(BudgetStatus::getCategory));
        return out;
    }
}
