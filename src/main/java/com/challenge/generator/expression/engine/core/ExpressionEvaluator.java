package com.challenge.generator.expression.engine.core;

import com.challenge.generator.expression.api.dto.EmailGenerationRequest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.grammar.Grammar;
import com.challenge.generator.expression.engine.parser.Tokenizer;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Parses expressions and evaluates them against input data to produce a list of emails. This class is
 * created as a bean in the configuration.
 */
@Slf4j
public record ExpressionEvaluator(ExpressionParser parser, ExpressionInterpreter interpreter) {

    public ExpressionEvaluator(Grammar grammar, FunctionRegistry registry, Tokenizer tokenizer) {
        this(new ExpressionParser(tokenizer, grammar), new ExpressionInterpreter(registry));
    }

    public List<String> generateResults(EmailGenerationRequest request) {
        log.atDebug().log("Evaluating expression: '{}' with inputs: {}", request.expression(), request.input().keySet());
        var abstractSyntaxTree = parser.parse(request.expression());
        return interpreter.evaluate(abstractSyntaxTree, EvaluationContext.from(request.input())).asList();
    }
}
