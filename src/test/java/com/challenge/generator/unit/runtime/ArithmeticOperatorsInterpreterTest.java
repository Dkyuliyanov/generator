package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("ArithmeticOperatorsInterpreter Tests")
class ArithmeticOperatorsInterpreterTest extends BaseTest {

    @Nested
    @DisplayName("Successful Evaluation")
    class SuccessTests {

        private record TestCase(String description, String expression, Map<String, Object> data, String expected) {
            Arguments toArg() {
                return Arguments.of(description, expression, data, expected);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.runtime.ArithmeticOperatorsInterpreterTest#successScenarios")
        void shouldEvaluateArithmeticExpressionsCorrectly(String description, String expression, Map<String, Object> data, String expected) {
            String result = evalFirst(expression, data);
            assertEquals(expected, result);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorTests {

        private record ErrorTestCase(String description, String expression, Class<? extends Throwable> expectedException) {
            Arguments toArg() {
                return Arguments.of(description, expression, expectedException);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.runtime.ArithmeticOperatorsInterpreterTest#errorScenarios")
        void shouldThrowExceptionForInvalidOperations(String description, String expression, Class<? extends Throwable> expectedException) {
            var ast = parser().parse(expression);
            assertThrows(expectedException, () -> expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of())));
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
                new SuccessTests.TestCase("should handle integer addition", "1 + 2", Map.of(), "3").toArg(),
                new SuccessTests.TestCase("should handle integer subtraction", "10 - 5", Map.of(), "5").toArg(),
                new SuccessTests.TestCase("should handle integer multiplication", "3 * 4", Map.of(), "12").toArg(),
                new SuccessTests.TestCase("should handle integer division", "10 / 2", Map.of(), "5").toArg(),
                new SuccessTests.TestCase("should handle decimal addition", "1.5 + 2.5", Map.of(), "4.0").toArg(),
                new SuccessTests.TestCase("should handle decimal subtraction", "10.0 - 5.5", Map.of(), "4.5").toArg(),
                new SuccessTests.TestCase("should handle mixed-type addition of integer and decimal", "1 + 2.5", Map.of(), "3.5").toArg(),
                new SuccessTests.TestCase("should handle mixed-type addition of identifier and integer", "age + 10", Map.of("age", 25), "35").toArg(),
                new SuccessTests.TestCase("should concatenate string and number with ampersand", "'hello' & 5", Map.of(), "hello5").toArg(),
                new SuccessTests.TestCase("should respect operator precedence (multiplication before addition)", "2 + 3 * 4", Map.of(), "14").toArg(),
                new SuccessTests.TestCase("should respect parentheses in expressions", "(2 + 3) * 4", Map.of(), "20").toArg()
        );
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
                new ErrorTests.ErrorTestCase("should throw for multiplying a string by a number", "'hello' * 5", UnsupportedOperationException.class).toArg(),
                new ErrorTests.ErrorTestCase("should throw for division by zero", "10 / 0", ArithmeticException.class).toArg()
        );
    }
}
