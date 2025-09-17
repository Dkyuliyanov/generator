package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.ast.LiteralNode;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.springframework.stereotype.Component;

/**
 * Provides parsing support for decimal literal tokens.
 */
@Component
public class DecimalLiteralRuleProvider implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.DECIMAL_LITERAL;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parsePrefix(ExpressionParser parser, TokenStream stream, Token token) {
                var numericValue = new java.math.BigDecimal(token.value());
                return new LiteralNode(numericValue, com.challenge.generator.expression.engine.model.DataType.DECIMAL, token.position());
            }
        };
    }
}
