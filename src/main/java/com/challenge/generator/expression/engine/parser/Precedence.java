package com.challenge.generator.expression.engine.parser;

public enum Precedence {
    ASSIGNMENT,
    TERNARY,
    COMPARISON,
    SUM,
    PRODUCT,
    PREFIX,
    CALL
}
