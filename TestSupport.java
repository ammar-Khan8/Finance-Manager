package com.pfbm.test;

import com.pfbm.util.AppLogger;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Gives each test its own temp directory so tests never touch the real data/ folder. */
final class TestSupport {
    private TestSupport() {}

    static Path freshDir() throws IOException {
        Path dir = Files.createTempDirectory("pfbm-test");
        AppLogger.setLogFile(dir.resolve("app.log"));
        return dir;
    }
}
