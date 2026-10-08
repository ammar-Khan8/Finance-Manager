package com.pfbm.model;

import com.pfbm.exception.ValidationException;

public enum Category {
    SALARY, FREELANCE, FOOD, RENT, TRANSPORT, UTILITIES, ENTERTAINMENT, HEALTH, OTHER;

    /** Case-insensitive parse with a friendly error message. */
    public static Category parse(String text) {
        if (text == null || text.isBlank()) {
            throw new ValidationException("Category is required");
        }
        try {
            return valueOf(text.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException e) {
            throw new ValidationException("Unknown category '" + text + "'");
        }
    }
}
