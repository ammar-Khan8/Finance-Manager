package com.pfbm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public abstract class Transaction {
    private final int id;
    private final int userId;
    private final BigDecimal amount;
    private final LocalDate date;
    private final Category category;
    private final String description;

    protected Transaction(int id, int userId, BigDecimal amount, LocalDate date,
                          Category category, String description) {
        this.id = id;
        this.userId = userId;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.description = description;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getDate() { return date; }
    public Category getCategory() { return category; }
    public String getDescription() { return description; }

    public abstract boolean isIncome();

    public String getType() {
        return isIncome() ? "INCOME" : "EXPENSE";
    }
}
