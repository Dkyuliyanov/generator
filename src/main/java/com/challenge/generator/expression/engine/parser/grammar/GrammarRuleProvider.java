package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.token.TokenType;

public interface GrammarRuleProvider {
    TokenType getTokenType();
    TokenParser getTokenParser();
}
