package com.pfbm.dao;

import com.pfbm.model.User;
import java.util.List;
import java.util.Optional;

public interface UserDAO {
    int nextId();
    void add(User user);
    List<User> findAll();
    Optional<User> findByUsername(String username);
}
