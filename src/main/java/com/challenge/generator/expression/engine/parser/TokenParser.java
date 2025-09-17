package com.challenge.generator.expression.engine.parser;

import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;

public interface TokenParser {
    default ExpressionNode parsePrefix(ExpressionParser parser, TokenStream stream, Token token) {
        throw new ExpressionParseException(ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, "Token cannot be used in a prefix position", token.position());
    }

    default ExpressionNode parseInfix(ExpressionParser parser, TokenStream stream, ExpressionNode left, Token token) {
        throw new ExpressionParseException(ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, "Token cannot be used in an infix position", token.position());
    }

    default int getPrecedence() {
        return 0;
    }
}
