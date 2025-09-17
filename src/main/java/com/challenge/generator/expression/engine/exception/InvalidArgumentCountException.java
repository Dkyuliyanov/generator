package com.challenge.generator.expression.engine.exception;

import lombok.Getter;

/**
 * Thrown when a function is called with an incorrect number of arguments.
 */
@Getter
public final class InvalidArgumentCountException extends EvaluationException {

    private final String functionName;
    private final int expected;
    private final int actual;

    public InvalidArgumentCountException(String functionName, int expected, int actual) {
        super(String.format("Function '%s' requires %d argument(s) but received %d", functionName, expected, actual));
        this.functionName = functionName;
        this.expected = expected;
        this.actual = actual;
    }
}
