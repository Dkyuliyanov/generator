package com.challenge.generator.expression.engine.exception;

import lombok.Getter;

/**
 * A unified exception for all errors occurring during the tokenization and parsing phases.
 */
@Getter
public final class ExpressionParseException extends EvaluationException {

    public enum ErrorCode {
        UNEXPECTED_CHARACTER,
        UNTERMINATED_STRING,
        INVALID_TOKEN_PLACEMENT,
        MISSING_EXPECTED_TOKEN,
        TRAILING_CHARACTERS,
        INVALID_FUNCTION_TARGET
    }

    private final ErrorCode errorCode;
    private final int position;

    public ExpressionParseException(ErrorCode errorCode, String message, int position) {
        super(message);
        this.errorCode = errorCode;
        this.position = position;
    }

}
