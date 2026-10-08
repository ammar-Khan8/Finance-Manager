package com.pfbm.model;

public class User {
    private final int id;
    private final String username;
    private final String salt;
    private final String passwordHash;

    public User(int id, String username, String salt, String passwordHash) {
        this.id = id;
        this.username = username;
        this.salt = salt;
        this.passwordHash = passwordHash;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getSalt() { return salt; }
    public String getPasswordHash() { return passwordHash; }
}
