package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Ternary with Comparison Operators Tests")
class TernaryComparisonTest extends BaseTest {

    @Test
    @DisplayName("Simple ternary with less than comparison")
    void testTernaryWithLessThan() {
        String expression = "age < '30' ? 'young' : 'mature'";
        var ast = parser().parse(expression);
        var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of("age", "25")));
        String first = result.firstOrNull();
        assertEquals("young", first);
    }

    @Test 
    @DisplayName("Ternary with comparison in concatenation")
    void testTernaryWithConcatenation() {
        String expression = "firstName & (age < '30' ? '.young' : '.mature')";
        var ast = parser().parse(expression);
        var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of("firstName", "John", "age", "25")));
        String first = result.firstOrNull();
        assertEquals("John.young", first);
    }
}
