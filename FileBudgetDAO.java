package com.pfbm.dao;

import com.pfbm.model.Budget;
import com.pfbm.model.Category;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/** budgets.csv: user_id,category,month,limit */
public class FileBudgetDAO implements BudgetDAO {
    private static final String HEADER = "user_id,category,month,limit";
    private final Path file;

    public FileBudgetDAO(Path file) {
        this.file = file;
    }

    @Override
    public void upsert(Budget b) {
        List<Budget> all = loadAll();
        all.removeIf(x -> x.getUserId() == b.getUserId()
                && x.getCategory() == b.getCategory()
                && x.getMonth().equals(b.getMonth()));
        all.add(b);
        CsvFile.rewrite(file, HEADER, all.stream().map(this::toRow).collect(Collectors.toList()));
    }

    @Override
    public List<Budget> findByUser(int userId) {
        return loadAll().stream()
                .filter(b -> b.getUserId() == userId)
                .collect(Collectors.toList());
    }

    private List<Budget> loadAll() {
        List<Budget> list = new ArrayList<>();
        for (String[] r : CsvFile.readRows(file)) {
            list.add(new Budget(
                    Integer.parseInt(r[0].trim()),
                    Category.valueOf(r[1].trim()),
                    YearMonth.parse(r[2].trim()),
                    new BigDecimal(r[3].trim())));
        }
        return list;
    }

    private String toRow(Budget b) {
        return String.join(",",
                String.valueOf(b.getUserId()),
                b.getCategory().name(),
                b.getMonth().toString(),
                b.getLimit().toPlainString());
    }
}
