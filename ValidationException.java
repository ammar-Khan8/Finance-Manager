package com.pfbm.exception;

/** Unchecked: thrown when user input breaks a business rule. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
