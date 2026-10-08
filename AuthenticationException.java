package com.pfbm.exception;

/** Checked: login failed. */
public class AuthenticationException extends Exception {
    public AuthenticationException(String message) {
        super(message);
    }
}
