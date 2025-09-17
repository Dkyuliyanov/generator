package com.challenge.generator.expression.engine.parser.token;

import lombok.Getter;

@Getter
public enum TokenType {
    STRING_LITERAL("^'[^']*'"),
    INTEGER_LITERAL("^\\d+"),
    DECIMAL_LITERAL("^\\d+\\.\\d+"),
    IDENTIFIER("^[a-zA-Z_][a-zA-Z0-9_]*"),
    LEFT_PAREN("^\\("),
    RIGHT_PAREN("^\\)"),
    COMMA("^,"),
    PLUS("^\\+"),
    MINUS("^-"),
    STAR("^\\*"),
    SLASH("^/"),
    AMPERSAND("^&"),
    QUESTION_MARK("^\\?"),
    COLON("^:"),
    LESS_THAN("^<"),
    GREATER_THAN("^>"),
    EQUALS("^="),
    LESS_THAN_OR_EQUAL("^<="),
    GREATER_THAN_OR_EQUAL("^>="),
    NOT_EQUALS("^!="),
    SKIPPED("^\\s+");

    private final String regex;

    TokenType(String regex) {
        this.regex = regex;
    }

}
