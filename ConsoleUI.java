package com.pfbm.ui;

import com.pfbm.exception.AuthenticationException;
import com.pfbm.exception.InvalidAmountException;
import com.pfbm.exception.ValidationException;
import com.pfbm.model.*;
import com.pfbm.service.*;
import com.pfbm.util.AppLogger;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/** All console input and output lives here. */
public class ConsoleUI {
    private final Scanner in = new Scanner(System.in);
    private final UserService userService;
    private final TransactionService txService;
    private final BudgetService budgetService;
    private final ReportService reportService;
    private User currentUser;

    public ConsoleUI(UserService userService, TransactionService txService,
                     BudgetService budgetService, ReportService reportService) {
        this.userService = userService;
        this.txService = txService;
        this.budgetService = budgetService;
        this.reportService = reportService;
    }

    public void run() {
        System.out.println("=== Personal Finance & Budget Manager ===");
        boolean running = true;
        while (running) {
            try {
                running = (currentUser == null) ? authMenu() : mainMenu();
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                AppLogger.error(e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        System.out.println("Goodbye!");
    }

    private boolean authMenu() throws Exception {
        System.out.println("\n1) Login\n2) Register\n9) Exit");
        switch (ask("Choose an option: ")) {
            case "1": login(); return true;
            case "2": register(); return true;
            case "9": return false;
            default: System.out.println("Invalid choice."); return true;
        }
    }

    private void login() throws AuthenticationException {
        String username = ask("Username: ");
        String password = ask("Password: ");
        currentUser = userService.login(username, password);
        System.out.println("Welcome back, " + currentUser.getUsername() + "!");
    }

    private void register() {
        String username = ask("Choose a username (3-30 chars): ");
        String password = ask("Choose a password (min 4 chars): ");
        currentUser = userService.register(username, password);
        System.out.println("Account created. You are logged in as " + currentUser.getUsername() + ".");
    }

    private boolean mainMenu() throws Exception {
        System.out.println("\n--- Menu (" + currentUser.getUsername() + ") ---");
        System.out.println("1) Add Income");
        System.out.println("2) Add Expense");
        System.out.println("3) List Transactions (with running balance)");
        System.out.println("4) Filter Transactions by Month");
        System.out.println("5) Set Monthly Budget");
        System.out.println("6) View Budgets & Alerts");
        System.out.println("7) Monthly Report (analytics)");
        System.out.println("8) Delete Transaction");
        System.out.println("0) Logout");
        System.out.println("9) Exit");
        switch (ask("Choose an option: ")) {
            case "1": addTransaction(true); break;
            case "2": addTransaction(false); break;
            case "3": listTransactions(); break;
            case "4": filterByMonth(); break;
            case "5": setBudget(); break;
            case "6": viewBudgets(); break;
            case "7": monthlyReport(); break;
            case "8": deleteTransaction(); break;
            case "0": currentUser = null; System.out.println("Logged out."); break;
            case "9": return false;
            default: System.out.println("Invalid choice.");
        }
        return true;
    }

    private void addTransaction(boolean income) throws InvalidAmountException {
        BigDecimal amount = TransactionService.parseAmount(ask("Amount: "));
        LocalDate date = askDate("Date (YYYY-MM-DD, blank = today): ");
        Category category = Category.parse(ask("Category (" + categoryList() + "): "));
        String description = ask("Description (optional): ");
        Transaction t = txService.add(currentUser.getId(), income, amount, date, category, description);
        System.out.println("Saved " + t.getType().toLowerCase() + " #" + t.getId() + " for " + money(amount) + ".");
    }

    private void listTransactions() {
        List<Transaction> all = txService.list(currentUser.getId());
        if (all.isEmpty()) {
            System.out.println("No transactions yet.");
            return;
        }
        printTable(all, all);
    }

    private void filterByMonth() {
        YearMonth month = askMonth("Month (YYYY-MM, blank = current): ");
        List<Transaction> all = txService.list(currentUser.getId());
        List<Transaction> rows = txService.listByMonth(currentUser.getId(), month);
        if (rows.isEmpty()) {
            System.out.println("No transactions in " + month + ".");
        } else {
            printTable(all, rows);
        }
    }

    /** Running balances are computed over the full history so filtered views stay accurate. */
    private void printTable(List<Transaction> all, List<Transaction> rows) {
        Map<Integer, BigDecimal> balances = new HashMap<>();
        BigDecimal running = BigDecimal.ZERO;
        for (Transaction t : all) {
            running = t.isIncome() ? running.add(t.getAmount()) : running.subtract(t.getAmount());
            balances.put(t.getId(), running);
        }
        System.out.printf("%-5s %-10s %-8s %-12s %14s %14s  %s%n",
                "ID", "Date", "Type", "Category", "Amount", "Balance", "Description");
        for (Transaction t : rows) {
            System.out.printf("%-5d %-10s %-8s %-12s %14s %14s  %s%n",
                    t.getId(), t.getDate(), t.getType(), t.getCategory(),
                    money(t.getAmount()), money(balances.get(t.getId())), t.getDescription());
        }
    }

    private void deleteTransaction() {
        int id = askInt("Transaction ID to delete: ");
        boolean ok = txService.delete(currentUser.getId(), id);
        System.out.println(ok ? "Transaction #" + id + " deleted." : "No transaction with ID " + id + " found.");
    }

    private void setBudget() throws InvalidAmountException {
        Category category = Category.parse(ask("Category (" + categoryList() + "): "));
        YearMonth month = askMonth("Month (YYYY-MM, blank = current): ");
        BigDecimal limit = TransactionService.parseAmount(ask("Monthly limit: "));
        budgetService.setBudget(currentUser.getId(), category, month, limit);
        System.out.println("Budget set: " + category + " " + money(limit) + " for " + month + ".");
    }

    private void viewBudgets() {
        YearMonth month = askMonth("Month (YYYY-MM, blank = current): ");
        List<BudgetStatus> statuses = budgetService.statusFor(currentUser.getId(), month);
        if (statuses.isEmpty()) {
            System.out.println("No budgets set for " + month + ".");
            return;
        }
        int alerts = 0;
        for (BudgetStatus s : statuses) {
            if (s.isOver()) {
                alerts++;
                System.out.printf("!! %-12s spent %s of %s  OVER BUDGET by %s%n",
                        s.getCategory(), money(s.getSpent()), money(s.getLimit()), money(s.getRemaining().abs()));
            } else {
                System.out.printf("   %-12s spent %s of %s  remaining %s%n",
                        s.getCategory(), money(s.getSpent()), money(s.getLimit()), money(s.getRemaining()));
            }
        }
        if (alerts > 0) {
            System.out.println("\n" + alerts + " budget alert(s) for " + month + ".");
        }
    }

    private void monthlyReport() {
        YearMonth month = askMonth("Month (YYYY-MM, blank = current): ");
        ReportService.MonthlySummary summary = reportService.summarize(currentUser.getId(), month);
        System.out.println("\nReport for " + month);
        System.out.println("  Income:   " + money(summary.getIncome()));
        System.out.println("  Expenses: " + money(summary.getExpense()));
        System.out.println("  Net:      " + money(summary.getNet()));

        Map<Category, BigDecimal> breakdown = reportService.expenseByCategory(currentUser.getId(), month);
        if (breakdown.isEmpty()) {
            System.out.println("No expenses this month.");
            return;
        }
        BigDecimal max = Collections.max(breakdown.values());
        System.out.println("\nSpending by category:");
        for (Map.Entry<Category, BigDecimal> e : breakdown.entrySet()) {
            int bars = max.signum() == 0 ? 0
                    : e.getValue().multiply(BigDecimal.valueOf(40)).divide(max, 0, RoundingMode.HALF_UP).intValue();
            System.out.printf("  %-12s |%-40s| %s%n", e.getKey(), "#".repeat(bars), money(e.getValue()));
        }
    }

    private int askInt(String prompt) {
        String text = ask(prompt);
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            throw new ValidationException("Please enter a whole number");
        }
    }

    private LocalDate askDate(String prompt) {
        String text = ask(prompt);
        if (text.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid date. Use YYYY-MM-DD, e.g. 2026-03-15");
        }
    }

    private YearMonth askMonth(String prompt) {
        String text = ask(prompt);
        if (text.isEmpty()) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(text);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Invalid month. Use YYYY-MM, e.g. 2026-03");
        }
    }

    private String categoryList() {
        return Arrays.stream(Category.values()).map(Enum::name).collect(Collectors.joining(", "));
    }

    private static String money(BigDecimal value) {
        return String.format("%,.2f", value);
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        if (!in.hasNextLine()) {
            System.out.println("\nGoodbye!");
            System.exit(0);
        }
        return in.nextLine().trim();
    }
}
