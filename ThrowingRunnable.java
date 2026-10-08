package com.pfbm.test;

@FunctionalInterface
interface ThrowingRunnable {
    void run() throws Exception;
}
