package com.pfbm.dao;

import com.pfbm.exception.DataAccessException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Small helper for reading and writing header-based CSV files. Every file has a header row. */
final class CsvFile {
    private CsvFile() {}

    static List<String[]> readRows(Path file) {
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(file)) {
            return rows;
        }
        try {
            List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {   // skip header
                String line = lines.get(i);
                if (!line.trim().isEmpty()) {
                    rows.add(line.split(",", -1));
                }
            }
            return rows;
        } catch (IOException e) {
            throw new DataAccessException("Cannot read " + file.getFileName(), e);
        }
    }

    static void append(Path file, String header, String line) {
        try {
            ensureParent(file);
            boolean fresh = !Files.exists(file) || Files.size(file) == 0;
            List<String> out = fresh ? Arrays.asList(header, line) : Collections.singletonList(line);
            Files.write(file, out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new DataAccessException("Cannot write " + file.getFileName(), e);
        }
    }

    static void rewrite(Path file, String header, List<String> lines) {
        try {
            ensureParent(file);
            List<String> all = new ArrayList<>();
            all.add(header);
            all.addAll(lines);
            Files.write(file, all, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new DataAccessException("Cannot rewrite " + file.getFileName(), e);
        }
    }

    /** Strips characters that would break the CSV format. */
    static String clean(String s) {
        return s == null ? "" : s.replaceAll("[,\r\n]", " ").trim();
    }

    private static void ensureParent(Path file) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
    }
}
