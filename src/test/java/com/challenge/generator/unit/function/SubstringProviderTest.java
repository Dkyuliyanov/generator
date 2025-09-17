package com.challenge.generator.unit.function;

import com.challenge.generator.expression.engine.function.provider.SubstringProvider;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("SubstringProvider Tests")
class SubstringProviderTest {

    private final SubstringProvider provider = new SubstringProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    private record TestCase(String description, String input, String start, String end, List<String> expected) {
        Arguments toArg() {
            return Arguments.of(description, input, start, end, expected);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("substringScenarios")
    void shouldExtractSubstringCorrectly(String description, String input, String start, String end, List<String> expected) {
        ExecutableExpressionNode function = provider.create(List.of(single(input), single(start), single(end)));
        assertEquals(expected, function.evaluate(emptyContext).asList());
    }

    static Stream<Arguments> substringScenarios() {
        return Stream.of(
                new TestCase("should return full string when indices cover entire input", "test", "1", "4", List.of("test")).toArg(),
                new TestCase("should return partial string for indices within range", "test", "1", "2", List.of("te")).toArg(),
                new TestCase("should return empty string when start index is greater than end index", "abc", "3", "2", List.of("")).toArg(),
                new TestCase("should clamp indices when they are out of bounds", "abc", "0", "99", List.of("abc")).toArg(),
                new TestCase("should return empty list for null input string", null, "1", "1", Collections.emptyList()).toArg(),
                new TestCase("should return empty list for empty input string", "", "1", "1", Collections.emptyList()).toArg(),
                new TestCase("should correctly handle unicode characters", "你好", "1", "1", List.of("你")).toArg()
        );
    }

    private static ExecutableExpressionNode single(String s) {
        return in -> new StringValue(s);
    }
}
