package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.Precedence;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.ast.BinaryOpNode;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.token.Operator;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.springframework.stereotype.Component;

/**
 * Provides parsing support for the ampersand concatenation operator.
 */
@Component
public class AmpersandOperatorRule implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.AMPERSAND;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parseInfix(ExpressionParser parser, TokenStream stream, ExpressionNode left, Token token) {
                var right = parser.parseExpression(stream, getPrecedence());
                return new BinaryOpNode(Operator.CONCATENATE.getSymbol(), left, right, token.position());
            }

            @Override
            public int getPrecedence() {
                return Precedence.SUM.ordinal();
            }
        };
    }
}
