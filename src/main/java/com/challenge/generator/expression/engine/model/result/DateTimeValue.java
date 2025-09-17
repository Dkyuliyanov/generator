package com.challenge.generator.expression.engine.model.result;

import com.challenge.generator.expression.engine.model.DataType;

import java.util.List;
import java.util.Objects;

/**
 * EvalResult implementation representing a Date/DateTime typed value.
 * Internally, we keep an ISO-8601 string representation to avoid leaking temporal types
 * across the engine. This preserves backwards compatibility with string-based outputs
 * while carrying a DATETIME data type.
 */
public record DateTimeValue(String isoString) implements EvalResult {
    public DateTimeValue(String isoString) {
        this.isoString = Objects.requireNonNull(isoString, "isoString");
    }

    @Override
    public String firstOrNull() {
        return isoString;
    }

    @Override
    public List<String> asList() {
        return List.of(isoString);
    }

    @Override
    public DataType dataType() {
        return DataType.DATETIME;
    }

    @Override
    public String toString() {
        return isoString;
    }
}
