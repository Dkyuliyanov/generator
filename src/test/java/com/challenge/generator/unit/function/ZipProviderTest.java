package com.challenge.generator.unit.function;

import com.challenge.generator.expression.engine.function.provider.ZipProvider;
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

@DisplayName("ZipProvider Tests")
class ZipProviderTest {

    private final ZipProvider provider = new ZipProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    private record TestCase(String description, List<ExecutableExpressionNode> inputs, List<String> expected) {
        Arguments toArg() {
            return Arguments.of(description, inputs, expected);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("zipScenarios")
    void shouldZipValuesCorrectly(String description, List<ExecutableExpressionNode> inputs, List<String> expected) {
        ExecutableExpressionNode function = provider.create(inputs);
        assertEquals(expected, function.evaluate(emptyContext).asList());
    }

    static Stream<Arguments> zipScenarios() {
        return Stream.of(
                new TestCase(
                        "should concatenate multiple single-value strings",
                        List.of(single("a"), single("b"), single("c")),
                        List.of("abc")
                ).toArg(),
                new TestCase(
                        "should zip a single value with a multi-value list",
                        List.of(multi("a", "b"), single("x")),
                        List.of("ax", "bx")
                ).toArg(),
                new TestCase(
                        "should concatenate multi-value lists of the same length element-wise",
                        List.of(multi("a", "b"), multi("1", "2")),
                        List.of("a1", "b2")
                ).toArg(),
                new TestCase(
                        "should truncate to the shortest list for multi-value lists of different lengths",
                        List.of(multi("a", "b", "c"), multi("1", "2")),
                        List.of("a1", "b2")
                ).toArg(),
                new TestCase(
                        "should treat null inputs as empty strings",
                        List.of(single(null), single("x"), single(null)),
                        List.of("x")
                ).toArg(),
                new TestCase(
                        "should treat an empty list input as an empty string when zipping",
                        List.of(single(""), single("y")),
                        List.of("y")
                ).toArg(),
                new TestCase(
                        "should return an empty list if any argument is an empty multi-value list",
                        List.of(multi(), single("z")),
                        Collections.emptyList()
                ).toArg(),
                new TestCase(
                        "should correctly zip more than two arguments with multiple multi-value lists",
                        List.of(multi("a", "b"), single("."), multi("1", "2"), single("@"), single("domain")),
                        List.of("a.1@domain", "b.2@domain")
                ).toArg(),
                new TestCase(
                        "should correctly handle a complex email generation scenario",
                        List.of(multi("jean", "j", "jeannot"), single("."), single("Mignard"), single("@"), single("example.com")),
                        List.of("jean.Mignard@example.com", "j.Mignard@example.com", "jeannot.Mignard@example.com")
                ).toArg(),
                new TestCase(
                        "should zip three multi-value lists, truncating to the shortest",
                        List.of(multi("a", "b", "c", "d"), multi("1", "2"), multi("x", "y", "z")),
                        List.of("a1x", "b2y")
                ).toArg()
        );
    }

    private static ExecutableExpressionNode single(String s) {
        return in -> new StringValue(s);
    }

    private static ExecutableExpressionNode multi(String... values) {
        return in -> new MultiValue(Arrays.asList(values));
    }
}
