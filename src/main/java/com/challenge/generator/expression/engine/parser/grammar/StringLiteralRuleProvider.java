package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.ast.LiteralNode;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.apache.commons.text.StringEscapeUtils;
import org.springframework.stereotype.Component;

/**
 * Provides parsing support for string literal tokens with escape sequence handling.
 */
@Component
public class StringLiteralRuleProvider implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.STRING_LITERAL;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parsePrefix(ExpressionParser parser, TokenStream stream, Token token) {
                var raw = token.value().substring(1, token.value().length() - 1);
                var value = StringEscapeUtils.unescapeJava(raw);
                return new LiteralNode(value, com.challenge.generator.expression.engine.model.DataType.STRING, token.position());
            }
        };
    }
}
