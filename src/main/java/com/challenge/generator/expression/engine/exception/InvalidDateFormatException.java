package com.challenge.generator.expression.engine.exception;

/**
 * Thrown when the date(format, value) function receives an unrecognized format pattern
 * or a value that does not conform to the provided pattern.
 */
public final class InvalidDateFormatException extends EvaluationException {
    public InvalidDateFormatException(String message) {
        super(message);
    }
}
