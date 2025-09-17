package com.challenge.generator.expression.engine.exception;

import lombok.Getter;

/**
 * Thrown when two or more FunctionProvider beans declare the same function name.
 * This is a startup-time (fail-fast) error to prevent ambiguous function resolution.
 */
@Getter
public final class DuplicateFunctionNameException extends EvaluationException {

    private final String functionName;
    private final String existingProvider;
    private final String newProvider;

    public DuplicateFunctionNameException(String functionName, String existingProvider, String newProvider) {
        super(String.format("Duplicate function name '%s'", functionName == null ? "<null>" : functionName));
        this.functionName = functionName;
        this.existingProvider = existingProvider;
        this.newProvider = newProvider;
    }

}
