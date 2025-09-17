package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.BooleanValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Comparison Operators Interpreter Tests")
class ComparisonOperatorsInterpreterTest extends BaseTest {

    @Nested
    @DisplayName("Successful Evaluation")
    class SuccessTests {

        private record TestCase(String description, String expression, Map<String, Object> data, boolean expected) {
            Arguments toArg() {
                return Arguments.of(description, expression, data, expected);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.runtime.ComparisonOperatorsInterpreterTest#successScenarios")
        void shouldEvaluateComparisonExpressionsCorrectly(String description, String expression, Map<String, Object> data, boolean expected) {
            boolean result = evalBoolean(expression, data);
            assertEquals(expected, result);
        }
    }

    private boolean evalBoolean(String expression, Map<String, Object> data) {
        var ast = parser().parse(expression);
        var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(data));
        assertInstanceOf(BooleanValue.class, result, "Result should be a BooleanValue for expression: " + expression);
        return ((BooleanValue) result).value();
    }

    static Stream<Arguments> successScenarios() {
        return Stream.of(

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than (true)", "5 < 10", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than (false)", "10 < 5", Map.of(), false).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than (equal)", "5 < 5", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than (true)", "10 > 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than (false)", "5 > 10", Map.of(), false).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than (equal)", "5 > 5", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer equals (true)", "5 = 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer equals (false)", "5 = 10", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than or equal (less)", "5 <= 10", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than or equal (equal)", "5 <= 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer less than or equal (false)", "10 <= 5", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than or equal (greater)", "10 >= 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than or equal (equal)", "5 >= 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer greater than or equal (false)", "5 >= 10", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer not equals (true)", "5 != 10", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle integer not equals (false)", "5 != 5", Map.of(), false).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle decimal less than", "5.5 < 10.2", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle decimal greater than", "10.2 > 5.5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle decimal equals", "5.5 = 5.5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle decimal not equals", "5.5 != 10.2", Map.of(), true).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle mixed integer/decimal less than", "5 < 10.2", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle mixed decimal/integer equals", "5.0 = 5", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle mixed decimal/integer not equals", "5.1 != 5", Map.of(), true).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle string less than", "'apple' < 'banana'", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle string greater than", "'banana' > 'apple'", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle string equals", "'apple' = 'apple'", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle string not equals", "'apple' != 'banana'", Map.of(), true).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle variable comparison", "x > y", Map.of("x", 10, "y", 5), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle string variable comparison", "name = 'Alice'", Map.of("name", "Alice"), true).toArg(),
                

                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle arithmetic before comparison", "5 + 3 > 7", Map.of(), true).toArg(),
                new ComparisonOperatorsInterpreterTest.SuccessTests.TestCase("should handle multiplication before comparison", "2 * 3 = 6", Map.of(), true).toArg()
        );
    }
}
