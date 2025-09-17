package com.challenge.generator.unit.expression;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class TokenizerTest extends BaseTest {

    @Nested
    @DisplayName("Successful Tokenization Scenarios")
    class SuccessTests {

        private record TestCase(String description, String expression, Consumer<List<Token>> assertions) {
            Arguments toArg() {
                return Arguments.of(description, expression, assertions);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.expression.TokenizerTest#successScenarios")
        void shouldTokenizeValidExpressionsCorrectly(String description, String expression, Consumer<List<Token>> assertions) {
            List<Token> tokens = tokenizer.tokenize(expression);
            assertions.accept(tokens);
        }
    }

    @Nested
    @DisplayName("Error Handling Scenarios")
    class ErrorTests {

        @ParameterizedTest(name = "should throw for unexpected character: ''{0}''")
        @ValueSource(strings = {"$", "#", "`"})
        void shouldThrowExceptionForUnexpectedCharacters(String invalidInput) {
            ExpressionParseException ex = assertThrows(ExpressionParseException.class, () -> tokenizer.tokenize(invalidInput));
            assertEquals(ExpressionParseException.ErrorCode.UNEXPECTED_CHARACTER, ex.getErrorCode());
            assertTrue(ex.getMessage().contains("Unexpected character"));
            assertEquals(0, ex.getPosition());
        }
    }

    static Stream<Arguments> successScenarios() {
        return Stream.of(
                new SuccessTests.TestCase(
                        "should tokenize string literals and a plus operator",
                        "'a' + 'b'",
                        tokens -> {
                            assertEquals(3, tokens.size());
                            assertToken(tokens.get(0), TokenType.STRING_LITERAL, "'a'");
                            assertToken(tokens.get(1), TokenType.PLUS, "+");
                            assertToken(tokens.get(2), TokenType.STRING_LITERAL, "'b'");
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should tokenize a function call with arguments",
                        "split('a,b', name)",
                        tokens -> {
                            assertEquals(6, tokens.size());
                            assertToken(tokens.get(0), TokenType.IDENTIFIER, "split");
                            assertToken(tokens.get(1), TokenType.LEFT_PAREN, "(");
                            assertToken(tokens.get(2), TokenType.STRING_LITERAL, "'a,b'");
                            assertToken(tokens.get(3), TokenType.COMMA, ",");
                            assertToken(tokens.get(4), TokenType.IDENTIFIER, "name");
                            assertToken(tokens.get(5), TokenType.RIGHT_PAREN, ")");
                        }
                ).toArg()
        );
    }

    private static void assertToken(Token token, TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.type());
        assertEquals(expectedValue, token.value());
    }
}
