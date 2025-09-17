package com.challenge.generator.expression.engine.exception;

import lombok.Getter;

/**
 * Thrown when a function is referenced that is not present in the registry.
 */
@Getter
public final class UnknownFunctionException extends EvaluationException {

    private final String functionName;
    private final int position;

    public UnknownFunctionException(String functionName, int position) {
        super("Unknown function '" + functionName + "'");
        this.functionName = functionName;
        this.position = position;
    }
}
