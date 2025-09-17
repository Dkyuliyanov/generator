package com.challenge.generator.expression.engine.parser.grammar;

import com.challenge.generator.expression.engine.parser.TokenParser;
import com.challenge.generator.expression.engine.parser.token.TokenType;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class Grammar {
    private final Map<TokenType, TokenParser> parsers = new EnumMap<>(TokenType.class);

    public Grammar(List<GrammarRuleProvider> ruleProviders) {
        if (ruleProviders != null) {
            for (GrammarRuleProvider provider : ruleProviders) {
                parsers.put(provider.getTokenType(), provider.getTokenParser());
            }
        }
    }

    public TokenParser getParser(TokenType type) {
        return parsers.get(type);
    }
}
