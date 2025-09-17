package com.challenge.generator.expression.engine.function;

import lombok.Getter;

/**
 * Enumeration of all available function names in the expression engine.
 */
@Getter
public enum FunctionName {
    CROSS_JOIN("cross_join"),
    DATE("date"),
    EQUALS("equals"),
    SPLIT("split"),
    SUBSTRING_OF("substring"),
    ZIP("zip");

    private final String name;

    FunctionName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
