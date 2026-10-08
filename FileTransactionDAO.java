package com.pfbm.dao;

import com.pfbm.model.*;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** transactions.csv: id,user_id,type,amount,date,category,description */
public class FileTransactionDAO implements TransactionDAO {
    private static final String HEADER = "id,user_id,type,amount,date,category,description";
    private final Path file;

    public FileTransactionDAO(Path file) {
        this.file = file;
    }

    @Override
    public int nextId() {
        return loadAll().stream().mapToInt(Transaction::getId).max().orElse(0) + 1;
    }

    @Override
    public void add(Transaction t) {
        CsvFile.append(file, HEADER, toRow(t));
    }

    @Override
    public List<Transaction> findByUser(int userId) {
        return loadAll().stream()
                .filter(t -> t.getUserId() == userId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteById(int userId, int id) {
        List<Transaction> all = loadAll();
        boolean removed = all.removeIf(t -> t.getUserId() == userId && t.getId() == id);
        if (removed) {
            CsvFile.rewrite(file, HEADER, all.stream().map(this::toRow).collect(Collectors.toList()));
        }
        return removed;
    }

    private List<Transaction> loadAll() {
        List<Transaction> list = new ArrayList<>();
        for (String[] r : CsvFile.readRows(file)) {
            list.add(fromRow(r));
        }
        return list;
    }

    private Transaction fromRow(String[] r) {
        int id = Integer.parseInt(r[0].trim());
        int userId = Integer.parseInt(r[1].trim());
        BigDecimal amount = new BigDecimal(r[3].trim());
        LocalDate date = LocalDate.parse(r[4].trim());
        Category category = Category.valueOf(r[5].trim());
        String description = r.length > 6 ? r[6] : "";
        return "INCOME".equals(r[2].trim())
                ? new Income(id, userId, amount, date, category, description)
                : new Expense(id, userId, amount, date, category, description);
    }

    private String toRow(Transaction t) {
        return String.join(",",
                String.valueOf(t.getId()),
                String.valueOf(t.getUserId()),
                t.getType(),
                t.getAmount().toPlainString(),
                t.getDate().toString(),
                t.getCategory().name(),
                CsvFile.clean(t.getDescription()));
    }
}
