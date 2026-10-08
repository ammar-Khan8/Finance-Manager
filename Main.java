package com.pfbm;

import com.pfbm.dao.FileBudgetDAO;
import com.pfbm.dao.FileTransactionDAO;
import com.pfbm.dao.FileUserDAO;
import com.pfbm.service.BudgetService;
import com.pfbm.service.ReportService;
import com.pfbm.service.TransactionService;
import com.pfbm.service.UserService;
import com.pfbm.ui.ConsoleUI;
import com.pfbm.util.AppLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Entry point: wires DAOs -> services -> UI. */
public class Main {
    public static void main(String[] args) {
        Path dataDir = Paths.get("data");
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            System.err.println("Cannot create data directory: " + e.getMessage());
            return;
        }
        AppLogger.setLogFile(dataDir.resolve("app.log"));

        FileTransactionDAO txDao = new FileTransactionDAO(dataDir.resolve("transactions.csv"));
        UserService users = new UserService(new FileUserDAO(dataDir.resolve("users.csv")));
        TransactionService txs = new TransactionService(txDao);
        BudgetService budgets = new BudgetService(new FileBudgetDAO(dataDir.resolve("budgets.csv")), txDao);
        ReportService reports = new ReportService(txDao);

        new ConsoleUI(users, txs, budgets, reports).run();
    }
}
