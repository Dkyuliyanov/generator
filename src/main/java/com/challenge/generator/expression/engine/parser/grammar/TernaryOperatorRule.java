package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.ast.ConditionalNode;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.Precedence;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.springframework.stereotype.Component;

@Component
public class TernaryOperatorRule implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.QUESTION_MARK;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parseInfix(ExpressionParser parser, TokenStream stream, ExpressionNode left, Token token) {
                ExpressionNode thenBranch = parser.parseExpression(stream, getPrecedence() - 1);
                stream.consume(TokenType.COLON);
                ExpressionNode elseBranch = parser.parseExpression(stream, getPrecedence() - 1);
                return new ConditionalNode(left, thenBranch, elseBranch);
            }

            @Override
            public int getPrecedence() {
                return Precedence.TERNARY.ordinal();
            }
        };
    }
}
