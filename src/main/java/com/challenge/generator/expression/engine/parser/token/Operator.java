package com.challenge.generator.expression.engine.parser.token;

import lombok.Getter;

@Getter
public enum Operator {
    PLUS("+"),
    MINUS("-"),
    MULTIPLY("*"),
    DIVIDE("/"),
    CONCATENATE("&"),
    LESS_THAN("<"),
    GREATER_THAN(">"),
    EQUALS("="),
    LESS_THAN_OR_EQUAL("<="),
    GREATER_THAN_OR_EQUAL(">="),
    NOT_EQUALS("!=");

    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public static Operator fromSymbol(String symbol) {
        for (Operator op : values()) {
            if (op.symbol.equals(symbol)) {
                return op;
            }
        }
        throw new IllegalArgumentException("Unknown operator symbol: " + symbol);
    }
}
