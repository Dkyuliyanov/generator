package com.challenge.generator.expression.engine.exception;

/**
 * Thrown when a conditional expression (?:) condition does not evaluate to a boolean value.
 */
public class InvalidConditionTypeException extends EvaluationException {
    public InvalidConditionTypeException(String message) {
        super(message);
    }
}
