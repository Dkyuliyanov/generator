package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ArithmeticTypeInference Tests")
class ArithmeticTypeInferenceTest extends BaseTest {

    private record TestCase(String description, String expression, DataType expectedType) {
        Arguments toArg() {
            return Arguments.of(description, expression, expectedType);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("typeInferenceScenarios")
    void shouldInferCorrectDataTypeForExpressions(String description, String expression, DataType expectedType) {
        var ast = parser().parse(expression);
        var result = expressionInterpreter.evaluate(ast, EvaluationContext.from(Map.of()));
        assertEquals(expectedType, result.dataType());
    }

    static Stream<Arguments> typeInferenceScenarios() {
        return Stream.of(
                new TestCase("should infer INTEGER for integer addition", "1 + 2", DataType.INTEGER).toArg(),
                new TestCase("should infer DECIMAL for mixed-type addition", "1 + 2.5", DataType.DECIMAL).toArg(),
                new TestCase("should infer INTEGER for integer division", "10 / 2", DataType.INTEGER).toArg(),
                new TestCase("should infer INTEGER for integer division with remainder", "10 / 4", DataType.INTEGER).toArg(),
                new TestCase("should infer STRING for ampersand concatenation", "'a' & 1", DataType.STRING).toArg(),
                new TestCase("should infer BOOLEAN for a true boolean function result", "equals('x','x')", DataType.BOOLEAN).toArg(),
                new TestCase("should infer BOOLEAN for a false boolean function result", "equals('x','y')", DataType.BOOLEAN).toArg()
        );
    }
}
