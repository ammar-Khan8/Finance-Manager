package com.pfbm.test;

import java.util.ArrayList;
import java.util.List;

/** Runs every test and exits non-zero on failure, so it works in CI. */
public final class RunAllTests {
    private RunAllTests() {}

    public static void main(String[] args) {
        List<TestCase> all = new ArrayList<>();
        all.addAll(UserServiceTest.cases());
        all.addAll(TransactionServiceTest.cases());
        all.addAll(BudgetServiceTest.cases());

        int passed = 0;
        int failed = 0;
        for (TestCase tc : all) {
            try {
                tc.body.run();
                passed++;
                System.out.println("[PASS] " + tc.name);
            } catch (Throwable t) {
                failed++;
                System.out.println("[FAIL] " + tc.name + " -> " + t.getMessage());
            }
        }
        System.out.println("\nTotal: " + all.size() + "  Passed: " + passed + "  Failed: " + failed);
        System.exit(failed == 0 ? 0 : 1);
    }
}
