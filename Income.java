package com.pfbm.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Income extends Transaction {
    public Income(int id, int userId, BigDecimal amount, LocalDate date,
                  Category category, String description) {
        super(id, userId, amount, date, category, description);
    }

    @Override
    public boolean isIncome() { return true; }
}
