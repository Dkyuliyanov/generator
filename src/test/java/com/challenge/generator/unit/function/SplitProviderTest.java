package com.challenge.generator.unit.function;

import com.challenge.generator.expression.engine.function.provider.SplitProvider;
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

@DisplayName("SplitProvider Tests")
class SplitProviderTest {

    private final SplitProvider provider = new SplitProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    private record TestCase(String description, String input, String delimiter, List<String> expected) {
        Arguments toArg() {
            return Arguments.of(description, input, delimiter, expected);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("splitScenarios")
    void shouldSplitStringCorrectly(String description, String input, String delimiter, List<String> expected) {
        ExecutableExpressionNode function = provider.create(List.of(single(input), single(delimiter)));
        assertEquals(expected, function.evaluate(emptyContext).asList());
    }

    static Stream<Arguments> splitScenarios() {
        return Stream.of(
                new TestCase("should split string by delimiter", "a,b,c", ",", List.of("a", "b", "c")).toArg(),
                new TestCase("should return original string in list if delimiter is not found", "abc", ",", List.of("abc")).toArg(),
                new TestCase("should return list of characters for empty string delimiter", "ab", "", List.of("a", "b")).toArg(),
                new TestCase("should return empty list for null input", null, ",", Collections.emptyList()).toArg(),
                new TestCase("should return empty list for empty input", "", ",", Collections.emptyList()).toArg(),
                new TestCase("should handle special regex character '.' as delimiter", "a.b.c", ".", List.of("a", "b", "c")).toArg(),
                new TestCase("should handle special regex character '|' as delimiter", "a|b|c", "|", List.of("a", "b", "c")).toArg(),
                new TestCase("should return multiple empty strings for input of only delimiters", ",,,", ",", List.of("", "", "", "")).toArg(),
                new TestCase("should handle leading and trailing delimiters correctly", ",a,b,", ",", List.of("", "a", "b", "")).toArg()
        );
    }

    private static ExecutableExpressionNode single(String s) {
        return in -> new StringValue(s);
    }
}
