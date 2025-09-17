package com.challenge.generator.expression.api.exception;

/**
 * Constants for validation error messages.
 */
public final class ValidationMessages {
    
    public static final String EXPRESSION_REQUIRED = "The 'expression' parameter must not be empty.";
    public static final String DYNAMIC_INPUT_REQUIRED = "At least one dynamic input parameter (e.g., 'lastName=doe') is required.";

    private ValidationMessages() {

    }
}
