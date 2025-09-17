package com.challenge.generator.unit.function;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.BooleanValue;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("EqualsProvider Tests")
class EqualsProviderTest extends BaseTest {

    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    private record TestCase(String description, String s1, String s2, boolean expected) {
        Arguments toArg() { return Arguments.of(description, s1, s2, expected); }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("equalsScenarios")
    void shouldCompareValuesCorrectly(String description, String s1, String s2, boolean expected) {
        ExecutableExpressionNode function = equalsProvider.create(List.of(single(s1), single(s2)));
        BooleanValue result = (BooleanValue) function.evaluate(emptyContext);
        assertEquals(expected, result.value());
    }

    static Stream<Arguments> equalsScenarios() {
        return Stream.of(
                new TestCase("should return true for identical strings", "abc", "abc", true).toArg(),
                new TestCase("should return false for different strings", "abc", "abd", false).toArg(),
                new TestCase("should return true for two null inputs", null, null, true).toArg(),
                new TestCase("should return false for one null and one non-null input", null, "x", false).toArg(),
                new TestCase("should be case-sensitive", "Abc", "abc", false).toArg(),
                new TestCase("should return true for two empty strings", "", "", true).toArg()
        );
    }

    private static ExecutableExpressionNode single(String s) {
        return in -> new StringValue(s);
    }
}
