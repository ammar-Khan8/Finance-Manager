package com.pfbm.test;

import com.pfbm.dao.FileUserDAO;
import com.pfbm.dao.UserDAO;
import com.pfbm.exception.AuthenticationException;
import com.pfbm.exception.ValidationException;
import com.pfbm.model.User;
import com.pfbm.service.UserService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

final class UserServiceTest {
    private UserServiceTest() {}

    static List<TestCase> cases() {
        List<TestCase> list = new ArrayList<>();

        list.add(new TestCase("register then login succeeds", () -> {
            UserService s = service(TestSupport.freshDir());
            s.register("alice", "secret1");
            Assert.equal("alice", s.login("alice", "secret1").getUsername(), "login should return alice");
        }));

        list.add(new TestCase("wrong password is rejected", () -> {
            UserService s = service(TestSupport.freshDir());
            s.register("alice", "secret1");
            Assert.throwsEx(AuthenticationException.class, () -> s.login("alice", "wrong!"));
        }));

        list.add(new TestCase("duplicate username is rejected (case-insensitive)", () -> {
            UserService s = service(TestSupport.freshDir());
            s.register("alice", "secret1");
            Assert.throwsEx(ValidationException.class, () -> s.register("ALICE", "other1"));
        }));

        list.add(new TestCase("password is stored salted and hashed, never in plain text", () -> {
            Path dir = TestSupport.freshDir();
            UserService s = service(dir);
            s.register("alice", "secret1");
            s.register("bob", "secret1");
            String raw = Files.readString(dir.resolve("users.csv"));
            Assert.isTrue(!raw.contains("secret1"), "plain-text password found on disk");
            List<User> users = new FileUserDAO(dir.resolve("users.csv")).findAll();
            Assert.isTrue(!users.get(0).getSalt().equals(users.get(1).getSalt()), "salts should differ");
            Assert.isTrue(!users.get(0).getPasswordHash().equals(users.get(1).getPasswordHash()),
                    "same password should hash differently with different salts");
        }));

        return list;
    }

    private static UserService service(Path dir) {
        UserDAO dao = new FileUserDAO(dir.resolve("users.csv"));
        return new UserService(dao);
    }
}
