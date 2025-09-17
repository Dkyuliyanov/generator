package com.challenge.generator.config;

import com.challenge.generator.expression.engine.core.ExpressionEvaluator;
import com.challenge.generator.expression.engine.core.FunctionRegistry;
import com.challenge.generator.expression.engine.parser.grammar.Grammar;
import com.challenge.generator.expression.engine.parser.grammar.GrammarRuleProvider;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import com.challenge.generator.expression.engine.parser.Tokenizer;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Spring configuration that wires the tokenizer, grammar, function registry,
 * and the ExpressionEvaluator for the expression language.
 */
@Configuration
public class ExpressionLanguageConfig {

    @Bean
    public List<Tokenizer.TokenInfo> expressionTokenDefinitions() {
        return List.of(
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.DECIMAL_LITERAL.getRegex()), TokenType.DECIMAL_LITERAL),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.INTEGER_LITERAL.getRegex()), TokenType.INTEGER_LITERAL),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.STRING_LITERAL.getRegex()), TokenType.STRING_LITERAL),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.IDENTIFIER.getRegex()), TokenType.IDENTIFIER),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.LEFT_PAREN.getRegex()), TokenType.LEFT_PAREN),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.RIGHT_PAREN.getRegex()), TokenType.RIGHT_PAREN),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.COMMA.getRegex()), TokenType.COMMA),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.PLUS.getRegex()), TokenType.PLUS),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.MINUS.getRegex()), TokenType.MINUS),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.STAR.getRegex()), TokenType.STAR),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.SLASH.getRegex()), TokenType.SLASH),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.AMPERSAND.getRegex()), TokenType.AMPERSAND),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.QUESTION_MARK.getRegex()), TokenType.QUESTION_MARK),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.COLON.getRegex()), TokenType.COLON),

                new Tokenizer.TokenInfo(Pattern.compile(TokenType.LESS_THAN_OR_EQUAL.getRegex()), TokenType.LESS_THAN_OR_EQUAL),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.GREATER_THAN_OR_EQUAL.getRegex()), TokenType.GREATER_THAN_OR_EQUAL),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.NOT_EQUALS.getRegex()), TokenType.NOT_EQUALS),

                new Tokenizer.TokenInfo(Pattern.compile(TokenType.LESS_THAN.getRegex()), TokenType.LESS_THAN),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.GREATER_THAN.getRegex()), TokenType.GREATER_THAN),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.EQUALS.getRegex()), TokenType.EQUALS),
                new Tokenizer.TokenInfo(Pattern.compile(TokenType.SKIPPED.getRegex()), TokenType.SKIPPED)
        );
    }

    @Bean
    public Tokenizer expressionTokenizer(List<Tokenizer.TokenInfo> tokenDefinitions) {
        return new Tokenizer(tokenDefinitions);
    }

    @Bean
    public Grammar expressionGrammar(List<GrammarRuleProvider> ruleProviders) {
        return new Grammar(ruleProviders);
    }

    @Bean
    public FunctionRegistry functionRegistry(List<FunctionProvider> providers) {
        return FunctionRegistry.fromProviders(providers);
    }

    @Bean
    public ExpressionEvaluator expressionEvaluator(Grammar grammar, FunctionRegistry registry, Tokenizer tokenizer) {
        return new ExpressionEvaluator(grammar, registry, tokenizer);
    }
}
