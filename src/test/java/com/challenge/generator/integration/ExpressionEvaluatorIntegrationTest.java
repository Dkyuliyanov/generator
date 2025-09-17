package com.challenge.generator.integration;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.api.dto.EmailGenerationRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ExpressionEvaluator Integration Tests")
class ExpressionEvaluatorIntegrationTest extends BaseTest {

    private record TestCase(String description, String expression, Map<String, Object> data, List<String> expectedResult) {
        Arguments toArg() {
            return Arguments.of(description, expression, data, expectedResult);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("expressionScenarios")
    void shouldEvaluateExpressionsCorrectly(String description, String expression, Map<String, Object> data, List<String> expectedResult) {
        EmailGenerationRequest request = new EmailGenerationRequest(expression, data);
        List<String> actualResult = expressionEvaluator.generateResults(request);
        assertEquals(expectedResult, actualResult);
    }

    private static Stream<Arguments> expressionScenarios() {
        return Stream.of(
                new TestCase(
                        "String literal concatenation",
                        "'a' & 'b'",
                        Map.of(),
                        List.of("ab")
                ).toArg(),
                new TestCase(
                        "Identifier concatenation",
                        "firstName & lastName",
                        Map.of("firstName", "john", "lastName", "doe"),
                        List.of("johndoe")
                ).toArg(),
                new TestCase(
                        "Arithmetic addition with concatenation",
                        "'result: ' & (5 + 3)",
                        Map.of(),
                        List.of("result: 8")
                ).toArg(),
                new TestCase(
                        "Substring with literal values",
                        "substring('abcdef', 2, 4)",
                        Map.of(),
                        List.of("bcd")
                ).toArg(),
                new TestCase(
                        "Substring with identifiers and concatenation",
                        "substring(firstName,1,1) & lastName & '@' & domain",
                        Map.of("firstName", "John", "lastName", "Doe", "domain", "company.com"),
                        List.of("JDoe@company.com")
                ).toArg()
        );
    }
}
