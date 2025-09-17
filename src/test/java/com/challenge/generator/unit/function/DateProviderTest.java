package com.challenge.generator.unit.function;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.exception.InvalidDateFormatException;
import com.challenge.generator.expression.engine.function.provider.DateProvider;
import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.DateTimeValue;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateProvider Tests")
class DateProviderTest extends BaseTest {

    private final DateProvider provider = new DateProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    @Nested
    @DisplayName("Successful Parsing")
    class SuccessTests {

        private record TestCase(String description, String pattern, String value, String expected) {
            Arguments toArg() {
                return Arguments.of(description, pattern, value, expected);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.function.DateProviderTest#dateScenarios")
        void shouldParseDateCorrectly(String description, String pattern, String value, String expected) {
            ExecutableExpressionNode function = provider.create(List.of(single(pattern), single(value)));
            DateTimeValue result = (DateTimeValue) function.evaluate(emptyContext);
            assertEquals(expected, result.firstOrNull());
            assertEquals(DataType.DATETIME, result.dataType());
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorTests {

        private record ErrorTestCase(String description, String pattern, String value) {
            Arguments toArg() {
                return Arguments.of(description, pattern, value);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.function.DateProviderTest#errorScenarios")
        void shouldThrowExceptionForInvalidInput(String description, String pattern, String value) {
            ExecutableExpressionNode function = provider.create(List.of(single(pattern), single(value)));
            assertThrows(InvalidDateFormatException.class, () -> function.evaluate(emptyContext));
        }
    }

    static Stream<Arguments> dateScenarios() {
        return Stream.of(
                new SuccessTests.TestCase("should parse a local date", "yyyy-MM-dd", "2025-01-02", "2025-01-02").toArg(),
                new SuccessTests.TestCase("should parse a local date-time", "yyyy-MM-dd HH:mm", "2025-01-02 07:30", "2025-01-02T07:30").toArg(),
                new SuccessTests.TestCase("should parse an offset date-time", "yyyy-MM-dd'T'HH:mmXXX", "2025-01-02T07:30+02:00", "2025-01-02T07:30+02:00").toArg()
        );
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
                new ErrorTests.ErrorTestCase("should throw for an invalid pattern", "not-a-pattern", "2025-01-02").toArg(),
                new ErrorTests.ErrorTestCase("should throw for a value that does not match the pattern", "yyyy-MM-dd", "01-02-2025").toArg()
        );
    }

    private static ExecutableExpressionNode single(String s) {
        return in -> new StringValue(s);
    }
}
