package com.pfbm.dao;

import com.pfbm.model.Budget;
import java.util.List;

public interface BudgetDAO {
    /** Insert, or replace an existing budget for the same user, category and month. */
    void upsert(Budget budget);
    List<Budget> findByUser(int userId);
}
