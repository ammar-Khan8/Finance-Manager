package com.pfbm.test;

import java.math.BigDecimal;
import java.util.Objects;

/** Minimal assertions, no external libraries. */
final class Assert {
    private Assert() {}

    static void isTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void equal(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " expected:<" + expected + "> but was:<" + actual + ">");
        }
    }

    static void money(String expected, BigDecimal actual) {
        if (new BigDecimal(expected).compareTo(actual) != 0) {
            throw new AssertionError("expected money " + expected + " but was " + actual);
        }
    }

    static void throwsEx(Class<? extends Throwable> type, ThrowingRunnable action) {
        try {
            action.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) {
                return;
            }
            throw new AssertionError("Expected " + type.getSimpleName() + " but got " + t);
        }
        throw new AssertionError("Expected " + type.getSimpleName() + " but nothing was thrown");
    }
}
