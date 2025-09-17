package com.challenge.generator.expression.engine.exception;

/**
 * Base class for custom exceptions in the evaluation subsystem.
 */
public abstract class EvaluationException extends RuntimeException {
    public EvaluationException(String message) {
        super(message);
    }
}
