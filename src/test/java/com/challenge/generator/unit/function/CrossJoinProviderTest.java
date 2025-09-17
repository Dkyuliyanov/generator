package com.challenge.generator.unit.function;

import com.challenge.generator.expression.engine.function.provider.CrossJoinProvider;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.MultiValue;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("CrossJoinProvider Tests")
class CrossJoinProviderTest {

    private final CrossJoinProvider provider = new CrossJoinProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    private record TestCase(String description, List<ExecutableExpressionNode> inputs, List<String> expected) {
        Arguments toArg() {
            return Arguments.of(description, inputs, expected);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("crossJoinScenarios")
    void shouldPerformCrossJoinCorrectly(String description, List<ExecutableExpressionNode> inputs, List<String> expected) {
        ExecutableExpressionNode function = provider.create(inputs);
        assertEquals(expected, function.evaluate(emptyContext).asList());
    }

    static Stream<Arguments> crossJoinScenarios() {
        return Stream.of(
                new TestCase(
                        "should perform cartesian product of two multi-value lists",
                        List.of(multi("a", "b"), multi("1", "2")),
                        List.of("a1", "a2", "b1", "b2")
                ).toArg(),
                new TestCase(
                        "should correctly join a single value with a multi-value list",
                        List.of(single(), multi("a", "b")),
                        List.of("xa", "xb")
                ).toArg(),
                new TestCase(
                        "should produce an empty result if any argument is an empty list",
                        List.of(multi("a"), multi()),
                        Collections.emptyList()
                ).toArg(),
                new TestCase(
                        "should treat null values in lists as empty strings",
                        List.of(multi(Arrays.asList("a", null)), multi("1")),
                        List.of("a1", "1")
                ).toArg(),
                new TestCase(
                        "should correctly join three or more lists",
                        List.of(multi("a", "b"), multi("1"), multi("X", "Y")),
                        List.of("a1X", "a1Y", "b1X", "b1Y")
                ).toArg()
        );
    }

    private static ExecutableExpressionNode single() {
        return in -> new StringValue("x");
    }

    private static ExecutableExpressionNode multi(String... values) {
        return in -> new MultiValue(Arrays.asList(values));
    }

    private static ExecutableExpressionNode multi(List<String> values) {
        return in -> new MultiValue(values);
    }
}
