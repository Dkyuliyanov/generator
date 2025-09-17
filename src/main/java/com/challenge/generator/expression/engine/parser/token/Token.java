package com.challenge.generator.expression.engine.parser.token;

/**
 * Token with its starting character position in the original expression.
 */
public record Token(TokenType type, String value, int position) {
}
