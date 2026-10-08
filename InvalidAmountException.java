package com.pfbm.exception;

/** Checked: an amount is not a positive number. */
public class InvalidAmountException extends Exception {
    public InvalidAmountException(String message) {
        super(message);
    }
}
