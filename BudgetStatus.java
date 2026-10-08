package com.pfbm.model;

import java.math.BigDecimal;

/** A budget combined with actual spending for the same category and month. */
public class BudgetStatus {
    private final Category category;
    private final BigDecimal limit;
    private final BigDecimal spent;

    public BudgetStatus(Category category, BigDecimal limit, BigDecimal spent) {
        this.category = category;
        this.limit = limit;
        this.spent = spent;
    }

    public Category getCategory() { return category; }
    public BigDecimal getLimit() { return limit; }
    public BigDecimal getSpent() { return spent; }
    public BigDecimal getRemaining() { return limit.subtract(spent); }
    public boolean isOver() { return spent.compareTo(limit) > 0; }
}
