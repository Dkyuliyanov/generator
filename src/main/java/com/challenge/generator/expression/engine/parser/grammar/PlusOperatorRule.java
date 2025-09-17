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

@Component
public class PlusOperatorRule implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.PLUS;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parseInfix(ExpressionParser parser, TokenStream stream, ExpressionNode left, Token token) {
                ExpressionNode right = parser.parseExpression(stream, getPrecedence());
                return new BinaryOpNode(Operator.PLUS.getSymbol(), left, right, token.position());
            }

            @Override
            public int getPrecedence() {
                return Precedence.SUM.ordinal();
            }
        };
    }
}
