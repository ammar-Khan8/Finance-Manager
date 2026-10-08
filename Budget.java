package com.pfbm.model;

import java.math.BigDecimal;
import java.time.YearMonth;

/** Monthly spending limit for one category, per user. */
public class Budget {
    private final int userId;
    private final Category category;
    private final YearMonth month;
    private final BigDecimal limit;

    public Budget(int userId, Category category, YearMonth month, BigDecimal limit) {
        this.userId = userId;
        this.category = category;
        this.month = month;
        this.limit = limit;
    }

    public int getUserId() { return userId; }
    public Category getCategory() { return category; }
    public YearMonth getMonth() { return month; }
    public BigDecimal getLimit() { return limit; }
}
