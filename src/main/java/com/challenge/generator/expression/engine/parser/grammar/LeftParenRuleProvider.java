package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.Precedence;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.springframework.stereotype.Component;

/**
 * Provides parsing support for left parentheses in expressions and function calls.
 */
@Component
public class LeftParenRuleProvider implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.LEFT_PAREN;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parsePrefix(ExpressionParser parser, TokenStream stream, Token token) {
                var expression = parser.parseExpression(stream, 0);
                stream.consume(TokenType.RIGHT_PAREN);
                return expression;
            }

            @Override
            public ExpressionNode parseInfix(ExpressionParser parser, TokenStream stream, ExpressionNode left, Token token) {
                return parser.parseFunctionCall(stream, left);
            }

            @Override
            public int getPrecedence() {
                return Precedence.CALL.ordinal();
            }
        };
    }
}
