package com.challenge.generator.expression.api.exception;

public class ValidationException extends BadRequestException {
    public ValidationException(String message) {
        super(message);
    }
}
