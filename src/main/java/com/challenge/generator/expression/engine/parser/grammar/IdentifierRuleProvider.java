package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.ast.IdentifierNode;
import com.challenge.generator.expression.engine.parser.ast.MapLiteralNode;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class IdentifierRuleProvider implements GrammarRuleProvider {
    @Override
    public TokenType getTokenType() {
        return TokenType.IDENTIFIER;
    }

    @Override
    public TokenParser getTokenParser() {
        return new TokenParser() {
            @Override
            public ExpressionNode parsePrefix(ExpressionParser parser, TokenStream stream, Token token) {
                if (stream.peek(TokenType.COLON)) {
                    return parseMapLiteral(stream, token);
                }
                return new IdentifierNode(token.value(), token.position());
            }

            private MapLiteralNode parseMapLiteral(TokenStream stream, Token firstKey) {
                Map<String, String> entries = new LinkedHashMap<>();
                String key = firstKey.value();

                do {
                    stream.consume(TokenType.COLON);
                    Token valueToken = stream.consume();
                    String value = parseMapValue(valueToken);
                    entries.put(key, value);

                    if (!stream.match(TokenType.COMMA)) {
                        break;
                    }

                    Token nextKeyToken = stream.consume();
                    if (nextKeyToken.type() != TokenType.IDENTIFIER) {
                        throw new ExpressionParseException(ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, "Expected identifier for map key", nextKeyToken.position());
                    }
                    key = nextKeyToken.value();

                } while (stream.peek(TokenType.COLON));

                return new MapLiteralNode(entries, firstKey.position());
            }

            private String parseMapValue(Token valueToken) {
                if (isMapValueToken(valueToken.type())) {
                    if (valueToken.type() == TokenType.STRING_LITERAL) {
                        return valueToken.value().substring(1, valueToken.value().length() - 1);
                    }
                    return valueToken.value();
                }
                throw new ExpressionParseException(ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, "Invalid map value", valueToken.position());
            }

            private boolean isMapValueToken(TokenType type) {
                return type == TokenType.IDENTIFIER ||
                        type == TokenType.STRING_LITERAL ||
                        type == TokenType.INTEGER_LITERAL ||
                        type == TokenType.DECIMAL_LITERAL;
            }
        };
    }
}
