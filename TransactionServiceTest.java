package com.pfbm.test;

import com.pfbm.dao.FileTransactionDAO;
import com.pfbm.exception.InvalidAmountException;
import com.pfbm.model.Category;
import com.pfbm.model.Transaction;
import com.pfbm.service.TransactionService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

final class TransactionServiceTest {
    private TransactionServiceTest() {}

    static List<TestCase> cases() {
        List<TestCase> list = new ArrayList<>();

        list.add(new TestCase("income and expenses update the balance", () -> {
            TransactionService s = service(TestSupport.freshDir());
            s.add(1, true, new BigDecimal("500"), LocalDate.of(2026, 1, 1), Category.SALARY, "pay");
            s.add(1, false, new BigDecimal("120"), LocalDate.of(2026, 1, 2), Category.FOOD, "groceries");
            Assert.money("380.00", s.balance(1));
        }));

        list.add(new TestCase("zero, negative and non-numeric amounts are rejected", () -> {
            TransactionService s = service(TestSupport.freshDir());
            Assert.throwsEx(InvalidAmountException.class,
                    () -> s.add(1, false, BigDecimal.ZERO, null, Category.FOOD, ""));
            Assert.throwsEx(InvalidAmountException.class, () -> TransactionService.parseAmount("-5"));
            Assert.throwsEx(InvalidAmountException.class, () -> TransactionService.parseAmount("abc"));
        }));

        list.add(new TestCase("filter by month returns only that month", () -> {
            TransactionService s = service(TestSupport.freshDir());
            s.add(1, false, new BigDecimal("10"), LocalDate.of(2026, 1, 10), Category.FOOD, "");
            s.add(1, false, new BigDecimal("20"), LocalDate.of(2026, 2, 5), Category.FOOD, "");
            Assert.equal(1, s.listByMonth(1, YearMonth.of(2026, 1)).size(), "January should have one transaction");
        }));

        list.add(new TestCase("delete removes a transaction; deleting a missing ID returns false", () -> {
            TransactionService s = service(TestSupport.freshDir());
            Transaction t = s.add(1, false, new BigDecimal("15"), LocalDate.of(2026, 3, 1), Category.FOOD, "");
            Assert.isTrue(s.delete(1, t.getId()), "first delete should succeed");
            Assert.isTrue(!s.delete(1, t.getId()), "second delete should report not found");
        }));

        list.add(new TestCase("transactions persist across service instances", () -> {
            Path dir = TestSupport.freshDir();
            service(dir).add(1, true, new BigDecimal("900"), LocalDate.of(2026, 4, 1), Category.SALARY, "april");
            Assert.equal(1, service(dir).list(1).size(), "transaction should be reloaded from CSV");
        }));

        return list;
    }

    private static TransactionService service(Path dir) {
        return new TransactionService(new FileTransactionDAO(dir.resolve("transactions.csv")));
    }
}
