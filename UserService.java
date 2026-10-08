package com.pfbm.service;

import com.pfbm.dao.UserDAO;
import com.pfbm.exception.AuthenticationException;
import com.pfbm.exception.ValidationException;
import com.pfbm.model.User;
import com.pfbm.util.AppLogger;
import com.pfbm.util.PasswordUtil;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {
    private static final Pattern USERNAME_RULE = Pattern.compile("[A-Za-z0-9_.-]{3,30}");
    private final UserDAO userDao;

    public UserService(UserDAO userDao) {
        this.userDao = userDao;
    }

    public User register(String username, String password) {
        String name = username == null ? "" : username.trim();
        if (!USERNAME_RULE.matcher(name).matches()) {
            throw new ValidationException("Username must be 3-30 characters: letters, digits, '_', '.', or '-'");
        }
        if (password == null || password.length() < 4) {
            throw new ValidationException("Password must be at least 4 characters");
        }
        if (userDao.findByUsername(name).isPresent()) {
            throw new ValidationException("Username already taken");
        }
        String salt = PasswordUtil.generateSalt();
        User user = new User(userDao.nextId(), name, salt, PasswordUtil.hash(password, salt));
        userDao.add(user);
        AppLogger.info("User registered: " + name);
        return user;
    }

    public User login(String username, String password) throws AuthenticationException {
        String name = username == null ? "" : username.trim();
        String pwd = password == null ? "" : password;
        Optional<User> found = userDao.findByUsername(name);
        if (!found.isPresent() || !PasswordUtil.verify(pwd, found.get().getSalt(), found.get().getPasswordHash())) {
            AppLogger.info("Failed login attempt for user: " + name);
            throw new AuthenticationException("Invalid username or password");
        }
        AppLogger.info("Login success: " + name);
        return found.get();
    }
}
