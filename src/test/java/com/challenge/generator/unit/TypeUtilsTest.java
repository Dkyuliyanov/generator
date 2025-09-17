package com.challenge.generator.unit;

import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.TypeUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TypeUtils Tests")
class TypeUtilsTest {

    @Nested
    @DisplayName("Integer Inference")
    class IntegerTests {
        @ParameterizedTest(name = "should infer ''{0}'' as INTEGER")
        @ValueSource(strings = {"0", "42", "-7", "001"})
        void shouldInferIntegersCorrectly(String input) {
            assertEquals(DataType.INTEGER, TypeUtils.inferType(input));
            assertTrue(TypeUtils.isInteger(input));
        }
    }

    @Nested
    @DisplayName("Decimal Inference")
    class DecimalTests {
        @ParameterizedTest(name = "should infer ''{0}'' as DECIMAL")
        @ValueSource(strings = {"1.0", "-3.14", "000.5"})
        void shouldInferDecimalsCorrectly(String input) {
            assertEquals(DataType.DECIMAL, TypeUtils.inferType(input));
            assertTrue(TypeUtils.isDecimal(input));
        }

        @ParameterizedTest(name = "should not infer ''{0}'' as DECIMAL")
        @ValueSource(strings = {"1.", ".5"})
        void shouldNotInferInvalidDecimals(String input) {
            assertNotEquals(DataType.DECIMAL, TypeUtils.inferType(input));
            assertFalse(TypeUtils.isDecimal(input));
        }
    }

    @Nested
    @DisplayName("Boolean Inference")
    class BooleanTests {
        @ParameterizedTest(name = "should infer ''{0}'' as BOOLEAN")
        @ValueSource(strings = {"true", "FALSE", "True"})
        void shouldInferBooleansCorrectly(String input) {
            assertEquals(DataType.BOOLEAN, TypeUtils.inferType(input));
            assertTrue(TypeUtils.isBoolean(input));
        }

        @ParameterizedTest(name = "should not infer ''{0}'' as BOOLEAN")
        @ValueSource(strings = {"truthy"})
        void shouldNotInferInvalidBooleans(String input) {
            assertNotEquals(DataType.BOOLEAN, TypeUtils.inferType(input));
            assertFalse(TypeUtils.isBoolean(input));
        }
    }

    @Nested
    @DisplayName("String and Unknown Inference")
    class OtherTests {

        private record TestCase(String description, String input, DataType expectedType) {
            Arguments toArg() {
                return Arguments.of(description, input, expectedType);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.TypeUtilsTest#stringAndUnknownScenarios")
        void shouldInferStringsAndUnknownCorrectly(String description, String input, DataType expectedType) {
            assertEquals(expectedType, TypeUtils.inferType(input));
        }
    }

    static Stream<Arguments> stringAndUnknownScenarios() {
        return Stream.of(
                new OtherTests.TestCase("should infer regular string as STRING", "hello", DataType.STRING).toArg(),
                new OtherTests.TestCase("should infer alphanumeric string as STRING", "123abc", DataType.STRING).toArg(),
                new OtherTests.TestCase("should infer null input as UNKNOWN", null, DataType.UNKNOWN).toArg()
        );
    }
}
