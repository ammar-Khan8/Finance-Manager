package com.pfbm.dao;

import com.pfbm.model.User;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** users.csv: id,username,salt,password_hash */
public class FileUserDAO implements UserDAO {
    private static final String HEADER = "id,username,salt,password_hash";
    private final Path file;

    public FileUserDAO(Path file) {
        this.file = file;
    }

    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        for (String[] r : CsvFile.readRows(file)) {
            users.add(new User(Integer.parseInt(r[0].trim()), r[1], r[2], r[3]));
        }
        return users;
    }

    @Override
    public int nextId() {
        return findAll().stream().mapToInt(User::getId).max().orElse(0) + 1;
    }

    @Override
    public void add(User user) {
        CsvFile.append(file, HEADER, String.join(",",
                String.valueOf(user.getId()), user.getUsername(), user.getSalt(), user.getPasswordHash()));
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return findAll().stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();
    }
}
