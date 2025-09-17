package com.challenge.generator.expression.engine.parser;

import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record Tokenizer(List<TokenInfo> tokenInfos) {

    public record TokenInfo(Pattern regex, TokenType type) {
    }

    public List<Token> tokenize(String str) {
        String input = Objects.toString(str, "");
        List<Token> tokens = new ArrayList<>();
        int position = 0;

        while (position < input.length()) {
            boolean matchFound = false;
            for (TokenInfo info : tokenInfos) {
                Matcher matcher = info.regex().matcher(input);
                matcher.region(position, input.length());

                if (matcher.lookingAt()) {
                    String value = matcher.group();
                    if (info.type() != TokenType.SKIPPED) {
                        tokens.add(new Token(info.type(), value, position));
                    }
                    position += value.length();
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                handleUnmatchedToken(input, position);
            }
        }
        return tokens;
    }

    private void handleUnmatchedToken(String input, int position) {
        String remainder = input.substring(position);
        if (remainder.startsWith("'")) {
            String message = "Unterminated string literal at position " + position;
            throw new ExpressionParseException(ExpressionParseException.ErrorCode.UNTERMINATED_STRING, message, position);
        }
        String message = "Unexpected character at position " + position;
        throw new ExpressionParseException(ExpressionParseException.ErrorCode.UNEXPECTED_CHARACTER, message, position);
    }
}
