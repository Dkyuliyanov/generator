package com.challenge.generator.expression.engine.parser.token;

import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import java.util.List;
import java.util.Objects;

import static com.challenge.generator.expression.engine.exception.ExpressionParseException.ErrorCode.MISSING_EXPECTED_TOKEN;

public class TokenStream {

    private final List<Token> tokens;
    private int position;

    public TokenStream(List<Token> tokens) {
        this.tokens = Objects.requireNonNullElse(tokens, List.of());
        this.position = 0;
    }

    public Token consume() {
        if (!hasMore()) {
            return createEofToken();
        }
        return tokens.get(position++);
    }

    public void consume(TokenType expected) {
        Token token = peek();
        if (token.type() != expected) {
            String found = Objects.toString(token.type(), "'" + token.value() + "'");
            String message = String.format("Expected %s but found %s", expected, found);
            throw new ExpressionParseException(MISSING_EXPECTED_TOKEN, message, token.position());
        }
        consume();
    }

    public boolean match(TokenType expected) {
        if (peek(expected)) {
            consume();
            return true;
        }
        return false;
    }

    public Token peek() {
        return hasMore() ? tokens.get(position) : createEofToken();
    }

    public boolean peek(TokenType expected) {
        return hasMore() && tokens.get(position).type() == expected;
    }

    public boolean hasMore() {
        return position < tokens.size();
    }

    private Token createEofToken() {
        int eofPos = 0;
        if (!tokens.isEmpty()) {
            Token last = tokens.getLast();
            eofPos = last.position() + Objects.toString(last.value(), "").length();
        }
        return new Token(null, "EOF", eofPos);
    }
}
