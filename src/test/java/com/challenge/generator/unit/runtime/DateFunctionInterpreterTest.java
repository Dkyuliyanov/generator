package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.exception.InvalidDateFormatException;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.DateTimeValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateFunctionInterpreter Tests")
class DateFunctionInterpreterTest extends BaseTest {

    @Nested
    @DisplayName("Successful Evaluation")
    class SuccessTests {

        private record TestCase(String description, String expression, String expected, Class<?> expectedType) {
            Arguments toArg() {
                return Arguments.of(description, expression, expected, expectedType);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.runtime.DateFunctionInterpreterTest#successScenarios")
        void shouldEvaluateDateExpressionsCorrectly(String description, String expression, String expected, Class<?> expectedType) {
            var result = eval(expression);
            assertEquals(expected, result.firstOrNull());
            assertInstanceOf(expectedType, result);
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorTests {

        @ParameterizedTest(name = "should throw for invalid date format: {0}")
        @ValueSource(strings = {"date('yyyy-MM','2025-01-02')"})
        void shouldThrowExceptionForInvalidDateOperations(String expression) {
            assertThrows(InvalidDateFormatException.class, () -> eval(expression));
        }
    }

    private com.challenge.generator.expression.engine.model.result.EvalResult eval(String expression) {
        var ast = parser().parse(expression);
        return expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of()));
    }

    static Stream<Arguments> successScenarios() {
        return Stream.of(
                new SuccessTests.TestCase(
                        "should produce a DateTimeValue for a local date",
                        "date('yyyy-MM-dd','2025-01-02')",
                        "2025-01-02",
                        DateTimeValue.class
                ).toArg(),
                new SuccessTests.TestCase(
                        "should return an ISO offset date-time for a date with time and offset",
                        "date('yyyy-MM-dd HH:mmXXX','2025-01-02 07:30+02:00')",
                        "2025-01-02T07:30+02:00",
                        DateTimeValue.class
                ).toArg(),
                new SuccessTests.TestCase(
                        "should allow a date to be concatenated as a string",
                        "'On ' & date('yyyy-MM-dd','2025-01-02')",
                        "On 2025-01-02",
                        com.challenge.generator.expression.engine.model.result.StringValue.class
                ).toArg()
        );
    }
}
