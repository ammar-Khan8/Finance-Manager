package com.pfbm.test;

import com.pfbm.dao.FileBudgetDAO;
import com.pfbm.dao.FileTransactionDAO;
import com.pfbm.model.BudgetStatus;
import com.pfbm.model.Category;
import com.pfbm.service.BudgetService;
import com.pfbm.service.TransactionService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

final class BudgetServiceTest {
    private BudgetServiceTest() {}

    static List<TestCase> cases() {
        List<TestCase> list = new ArrayList<>();

        list.add(new TestCase("spending above the limit raises an over-budget alert", () -> {
            Path dir = TestSupport.freshDir();
            TransactionService tx = new TransactionService(new FileTransactionDAO(dir.resolve("transactions.csv")));
            BudgetService budgets = budgetService(dir);
            tx.add(1, false, new BigDecimal("1200"), LocalDate.of(2026, 3, 5), Category.FOOD, "dinner party");
            budgets.setBudget(1, Category.FOOD, YearMonth.of(2026, 3), new BigDecimal("1000"));
            List<BudgetStatus> status = budgets.statusFor(1, YearMonth.of(2026, 3));
            Assert.equal(1, status.size(), "one budget expected");
            Assert.isTrue(status.get(0).isOver(), "FOOD should be over budget");
        }));

        list.add(new TestCase("setting a budget again overwrites the old limit", () -> {
            BudgetService budgets = budgetService(TestSupport.freshDir());
            budgets.setBudget(1, Category.FOOD, YearMonth.of(2026, 3), new BigDecimal("1000"));
            budgets.setBudget(1, Category.FOOD, YearMonth.of(2026, 3), new BigDecimal("500"));
            List<BudgetStatus> status = budgets.statusFor(1, YearMonth.of(2026, 3));
            Assert.equal(1, status.size(), "should still be one budget for FOOD in March");
            Assert.money("500.00", status.get(0).getLimit());
        }));

        return list;
    }

    private static BudgetService budgetService(Path dir) {
        return new BudgetService(
                new FileBudgetDAO(dir.resolve("budgets.csv")),
                new FileTransactionDAO(dir.resolve("transactions.csv")));
    }
}
