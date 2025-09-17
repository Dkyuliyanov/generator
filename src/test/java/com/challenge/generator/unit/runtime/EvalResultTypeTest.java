package com.challenge.generator.unit.runtime;

import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.result.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EvalResultType Tests")
class EvalResultTypeTest {

    private record TestCase(String description, EvalResult result, DataType expectedType) {
        Arguments toArg() {
            return Arguments.of(description, result, expectedType);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("dataTypeScenarios")
    void shouldReturnCorrectDataTypeForVariousResults(String description, EvalResult result, DataType expectedType) {
        assertEquals(expectedType, result.dataType());
    }

    static Stream<Arguments> dataTypeScenarios() {
        return Stream.of(
                new TestCase("should identify an integer", new NumberValue(new java.math.BigDecimal("42")), DataType.INTEGER).toArg(),
                new TestCase("should identify a decimal", new NumberValue(new java.math.BigDecimal("3.14")), DataType.DECIMAL).toArg(),
                new TestCase("should identify a string", new StringValue("abc"), DataType.STRING).toArg(),
                new TestCase("should identify a boolean", new BooleanValue(true), DataType.BOOLEAN).toArg(),
                new TestCase("should identify an empty result as UNKNOWN", EvalResult.EMPTY, DataType.UNKNOWN).toArg(),
                new TestCase("should identify a multi-value list by its first element", new MultiValue(List.of("7", "8")), DataType.INTEGER).toArg()
        );
    }
}
