package com.pfbm.exception;

/** Unchecked: thrown when persistence fails (file I/O or malformed data). */
public class DataAccessException extends RuntimeException {
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
