package com.pfbm.test;

final class TestCase {
    final String name;
    final ThrowingRunnable body;

    TestCase(String name, ThrowingRunnable body) {
        this.name = name;
        this.body = body;
    }
}
