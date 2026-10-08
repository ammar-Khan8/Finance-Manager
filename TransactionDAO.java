package com.pfbm.dao;

import com.pfbm.model.Transaction;
import java.util.List;

public interface TransactionDAO {
    int nextId();
    void add(Transaction transaction);
    List<Transaction> findByUser(int userId);
    boolean deleteById(int userId, int id);
}
