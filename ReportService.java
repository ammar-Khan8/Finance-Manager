package com.pfbm.service;

import com.pfbm.dao.TransactionDAO;
import com.pfbm.model.Category;
import com.pfbm.model.Transaction;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.Map;

public class ReportService {
    private final TransactionDAO txDao;

    public ReportService(TransactionDAO txDao) {
        this.txDao = txDao;
    }

    public MonthlySummary summarize(int userId, YearMonth month) {
        BigDecimal income = BigDecimal.ZERO;
        BigDecimal expense = BigDecimal.ZERO;
        for (Transaction t : txDao.findByUser(userId)) {
            if (YearMonth.from(t.getDate()).equals(month)) {
                if (t.isIncome()) {
                    income = income.add(t.getAmount());
                } else {
                    expense = expense.add(t.getAmount());
                }
            }
        }
        return new MonthlySummary(income, expense);
    }

    public Map<Category, BigDecimal> expenseByCategory(int userId, YearMonth month) {
        Map<Category, BigDecimal> totals = new EnumMap<>(Category.class);
        for (Transaction t : txDao.findByUser(userId)) {
            if (!t.isIncome() && YearMonth.from(t.getDate()).equals(month)) {
                totals.merge(t.getCategory(), t.getAmount(), BigDecimal::add);
            }
        }
        return totals;
    }

    public static class MonthlySummary {
        private final BigDecimal income;
        private final BigDecimal expense;

        public MonthlySummary(BigDecimal income, BigDecimal expense) {
            this.income = income;
            this.expense = expense;
        }

        public BigDecimal getIncome() { return income; }
        public BigDecimal getExpense() { return expense; }
        public BigDecimal getNet() { return income.subtract(expense); }
    }
}
