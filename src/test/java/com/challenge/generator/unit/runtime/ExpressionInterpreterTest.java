package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpressionInterpreter Tests")
class ExpressionInterpreterTest extends BaseTest {

    @Nested
    @DisplayName("Successful Evaluation")
    class SuccessTests {

        private record TestCase(String description, String expression, Map<String, Object> data, String expected) {
            Arguments toArg() {
                return Arguments.of(description, expression, data, expected);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.runtime.ExpressionInterpreterTest#successScenarios")
        void shouldEvaluateExpressionsCorrectly(String description, String expression, Map<String, Object> data, String expected) {
            String result = evalFirst(expression, data);
            assertEquals(expected, result);
        }

        @Test
        void shouldPropagateEmptyResultsFromNestedFunctionsCorrectly() {
            var ast = parser().parse("split('', ',') & 'x'");
            var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of()));
            assertTrue(result.asList().isEmpty());
            assertNull(result.firstOrNull());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorTests {

        @ParameterizedTest(name = "should fail ternary for non-boolean condition: {0}")
        @ValueSource(strings = {"'x' ? 'yes' : 'no'", "'' ? 'yes' : 'no'"})
        void shouldFailTernaryOperatorWhenConditionIsNotBoolean(String expression) {
            assertThrows(RuntimeException.class, () -> evalFirst(expression, Map.of()));
        }
    }

    private String evalFirst(String expression, Map<String, Object> data) {
        var ast = parser().parse(expression);
        var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(data));
        String first = result.firstOrNull();
        return first == null ? "" : first;
    }

    static Stream<Arguments> successScenarios() {
        return Stream.of(
                new SuccessTests.TestCase(
                        "should evaluate deeply nested function calls correctly",
                        "split('a,b', ',') & split('x,y', ',')",
                        Map.of(),
                        "ax"
                ).toArg(),
                new SuccessTests.TestCase(
                        "should evaluate complex expressions with mixed operators and functions",
                        "'Hello ' & split('world', '') & '!'",
                        Map.of(),
                        "Hello w!"
                ).toArg(),
                new SuccessTests.TestCase(
                        "should handle a missing identifier gracefully by returning an empty string",
                        "'Hi ' & first & ' ' & last",
                        Map.of("first", "Ada"),
                        "Hi Ada "
                ).toArg(),
                new SuccessTests.TestCase(
                        "should handle a function returning boolean 'true' in a ternary condition",
                        "equals('a','a') ? 'T' : 'F'",
                        Map.of(),
                        "T"
                ).toArg(),
                new SuccessTests.TestCase(
                        "should handle a function returning boolean 'false' in a ternary condition",
                        "equals('a','b') ? 'T' : 'F'",
                        Map.of(),
                        "F"
                ).toArg(),
                new SuccessTests.TestCase(
                        "should compare evaluated arithmetic results in equals",
                        "equals(2+3, 5)",
                        Map.of(),
                        "true"
                ).toArg(),
                new SuccessTests.TestCase(
                        "should return false when evaluated arithmetic results are not equal",
                        "equals(2+3, 6)",
                        Map.of(),
                        "false"
                ).toArg()
        );
    }
}
