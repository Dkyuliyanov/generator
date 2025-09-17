package com.challenge.generator.expression.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Base API exception carrying an HTTP status and a safe client-facing message.
 */
@Getter
public abstract class ApiException extends RuntimeException {
    private final HttpStatus status;

    protected ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
