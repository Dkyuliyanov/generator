package com.challenge.generator.e2e;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.api.dto.EmailData;
import com.challenge.generator.expression.api.dto.EmailListResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("E2E: URL Encoding Interception")
public class UrlEncodingInterceptionTest extends BaseTest {

    private record TestCase(String description, String expression, Map<String, String> params, String expectedValue) {
        public Arguments toArg() {
            return Arguments.of(description, expression, params, expectedValue);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("urlHandlingScenarios")
    void expressionPreprocessor_handlesUrlEncodedExpressions(
            String description,
            String expressionWithSpaces,
            Map<String, String> params,
            String expectedValue) {

        URI uri = buildUri(expressionWithSpaces, params);
        EmailListResponse body = getOk(uri);
        List<EmailData> data = body.data();

        assertEquals(1, data.size());
        assertEquals(expectedValue, data.getFirst().value());
    }

    static Stream<Arguments> urlHandlingScenarios() {
        return Stream.of(
                new TestCase(
                        "Should interpret space as concatenation operator",
                        "firstName & lastName",
                        Map.of("firstName", "John", "lastName", "Doe"),
                        "JohnDoe"
                ).toArg(),
                new TestCase(
                        "Should interpret space as arithmetic plus operator",
                        "base + bonus * 2 - deduction",
                        Map.of("base", "100", "bonus", "50", "deduction", "20"),
                        "180"
                ).toArg(),
                new TestCase(
                        "Should handle mixed function and arithmetic operators with spaces",
                        "firstName & '@' & domain & '.' & (year + 1)",
                        Map.of("firstName", "Alice", "domain", "example.com", "year", "2023"),
                        "Alice@example.com.2024"
                ).toArg(),
                new TestCase(
                        "Should not modify correctly formatted expressions without spaces",
                        "firstName&'.'&lastName&'@'&domain",
                        Map.of("firstName", "Bob", "lastName", "Smith", "domain", "example.com"),
                        "Bob.Smith@example.com"
                ).toArg()
        );
    }
}
