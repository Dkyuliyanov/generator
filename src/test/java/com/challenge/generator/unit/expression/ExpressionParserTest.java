package com.challenge.generator.unit.expression;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.ast.BinaryOpNode;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.ast.FunctionCall;
import com.challenge.generator.expression.engine.parser.ast.IdentifierNode;
import com.challenge.generator.expression.engine.parser.ast.LiteralNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionParserTest extends BaseTest {

    @Nested
    @DisplayName("Successful Parsing Scenarios")
    class SuccessTests {

        private record TestCase(String description, String expression, Consumer<ExpressionNode> assertions) {
            Arguments toArg() {
                return Arguments.of(description, expression, assertions);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.expression.ExpressionParserTest#successScenarios")
        void shouldParseValidExpressionsCorrectly(String description, String expression, Consumer<ExpressionNode> assertions) {
            ExpressionNode result = parser().parse(expression);
            assertions.accept(result);
        }
    }

    @Nested
    @DisplayName("Error Handling Scenarios")
    class ErrorTests {

        private record ErrorTestCase(String description, String expression, ExpressionParseException.ErrorCode expectedErrorCode, int expectedPosition) {
            Arguments toArg() {
                return Arguments.of(description, expression, expectedErrorCode, expectedPosition);
            }
        }

        @ParameterizedTest(name = "[{index}] {0}")
        @MethodSource("com.challenge.generator.unit.expression.ExpressionParserTest#errorScenarios")
        void shouldThrowExceptionForInvalidExpressions(String description, String expression, ExpressionParseException.ErrorCode expectedErrorCode, int expectedPosition) {
            ExpressionParseException ex = assertThrows(ExpressionParseException.class, () -> parser().parse(expression));
            assertEquals(expectedErrorCode, ex.getErrorCode());
            if (expectedPosition >= 0) {
                assertEquals(expectedPosition, ex.getPosition());
            }
        }
    }

    static Stream<Arguments> successScenarios() {
        return Stream.of(
                new SuccessTests.TestCase(
                        "should parse a simple identifier",
                        "firstName",
                        node -> {
                            IdentifierNode id = assertInstanceOf(IdentifierNode.class, node);
                            assertEquals("firstName", id.name());
                            assertEquals(0, id.position());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should treat null input as an empty literal",
                        null,
                        node -> {
                            LiteralNode lit = assertInstanceOf(LiteralNode.class, node);
                            assertEquals("", lit.value());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should parse a string literal and unescape sequences",
                        "'a\\n\\t\\\\'",
                        node -> {
                            LiteralNode lit = assertInstanceOf(LiteralNode.class, node);
                            assertEquals("a\n\t\\", lit.value());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should build a binary op node for concatenation",
                        "'a' & 'b'",
                        node -> {
                            BinaryOpNode bin = assertInstanceOf(BinaryOpNode.class, node);
                            assertEquals("&", bin.operator());
                            assertEquals("a", ((LiteralNode) bin.left()).value());
                            assertEquals("b", ((LiteralNode) bin.right()).value());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should respect precedence with parentheses",
                        "'a' & ('b' & 'c')",
                        node -> {
                            BinaryOpNode outer = assertInstanceOf(BinaryOpNode.class, node);
                            assertEquals("&", outer.operator());
                            assertEquals("a", ((LiteralNode) outer.left()).value());
                            BinaryOpNode inner = assertInstanceOf(BinaryOpNode.class, outer.right());
                            assertEquals("&", inner.operator());
                            assertEquals("b", ((LiteralNode) inner.left()).value());
                            assertEquals("c", ((LiteralNode) inner.right()).value());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should parse a simple function call with no arguments",
                        "f()",
                        node -> {
                            FunctionCall call = assertInstanceOf(FunctionCall.class, node);
                            assertEquals("f", call.name());
                            assertTrue(call.arguments().isEmpty());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should parse a function with mixed literal and identifier arguments",
                        "g('a', name)",
                        node -> {
                            FunctionCall call = assertInstanceOf(FunctionCall.class, node);
                            assertEquals("g", call.name());
                            assertEquals(2, call.arguments().size());
                            assertEquals("a", ((LiteralNode) call.arguments().get(0)).value());
                            assertEquals("name", ((IdentifierNode) call.arguments().get(1)).name());
                        }
                ).toArg(),
                new SuccessTests.TestCase(
                        "should parse nested functions and operators in arguments",
                        "outer(inner('x') & 'y')",
                        node -> {
                            FunctionCall outer = assertInstanceOf(FunctionCall.class, node);
                            assertEquals("outer", outer.name());
                            BinaryOpNode ampersand = assertInstanceOf(BinaryOpNode.class, outer.arguments().getFirst());
                            FunctionCall inner = assertInstanceOf(FunctionCall.class, ampersand.left());
                            assertEquals("inner", inner.name());
                            assertEquals("x", ((LiteralNode) inner.arguments().getFirst()).value());
                            assertEquals("y", ((LiteralNode) ampersand.right()).value());
                        }
                ).toArg()
        );
    }

    static Stream<Arguments> errorScenarios() {
        return Stream.of(
                new ErrorTests.ErrorTestCase("Expression starts with a binary operator", "+ 'a'", ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, 0).toArg(),
                new ErrorTests.ErrorTestCase("Missing right parenthesis in function call", "split('a,b'", ExpressionParseException.ErrorCode.MISSING_EXPECTED_TOKEN, -1).toArg(),
                new ErrorTests.ErrorTestCase("Missing left parenthesis in function call", "split 'a,b')", ExpressionParseException.ErrorCode.TRAILING_CHARACTERS, -1).toArg(),
                new ErrorTests.ErrorTestCase("Comma used outside function arguments", "'a', 'b'", ExpressionParseException.ErrorCode.TRAILING_CHARACTERS, -1).toArg(),
                new ErrorTests.ErrorTestCase("Function call has a trailing comma", "split('a,b',)", ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, -1).toArg(),
                new ErrorTests.ErrorTestCase("Ternary operator is missing a colon", "'a' ? 'b' 'c'", ExpressionParseException.ErrorCode.MISSING_EXPECTED_TOKEN, -1).toArg(),
                new ErrorTests.ErrorTestCase("Ternary operator is missing the else clause", "'a' ? 'b' :", ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, -1).toArg(),
                new ErrorTests.ErrorTestCase("Unterminated string literal", "'abc", ExpressionParseException.ErrorCode.UNTERMINATED_STRING, -1).toArg(),
                new ErrorTests.ErrorTestCase("Consecutive binary operators", "'a' + + 'b'", ExpressionParseException.ErrorCode.INVALID_TOKEN_PLACEMENT, -1).toArg(),
                new ErrorTests.ErrorTestCase("Consecutive literals without an operator", "'a' 'b'", ExpressionParseException.ErrorCode.TRAILING_CHARACTERS, -1).toArg(),
                new ErrorTests.ErrorTestCase("Consecutive identifiers without an operator", "first last", ExpressionParseException.ErrorCode.TRAILING_CHARACTERS, -1).toArg(),
                new ErrorTests.ErrorTestCase("Invalid function call target", "'a'('b')", ExpressionParseException.ErrorCode.INVALID_FUNCTION_TARGET, -1).toArg(),
                new ErrorTests.ErrorTestCase("Missing right parenthesis with specific position", "func('a'", ExpressionParseException.ErrorCode.MISSING_EXPECTED_TOKEN, 8).toArg()
        );
    }
}
