package com.challenge.generator.expression.engine.parser;

import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import com.challenge.generator.expression.engine.parser.ast.FunctionCall;
import com.challenge.generator.expression.engine.parser.ast.IdentifierNode;
import com.challenge.generator.expression.engine.parser.ast.LiteralNode;
import com.challenge.generator.expression.engine.parser.grammar.Grammar;
import com.challenge.generator.expression.engine.parser.token.Token;
import com.challenge.generator.expression.engine.parser.token.TokenStream;
import com.challenge.generator.expression.engine.parser.token.TokenType;
import java.util.ArrayList;
import java.util.List;

import static com.challenge.generator.expression.engine.exception.ExpressionParseException.ErrorCode.*;
import static com.challenge.generator.expression.engine.model.DataType.STRING;

public record ExpressionParser(Tokenizer tokenizer, Grammar grammar) {

    public ExpressionNode parse(String expression) {
        if (expression == null || expression.isBlank()) {
            return new LiteralNode("", STRING, 0);
        }
        List<Token> tokens = tokenizer.tokenize(expression);
        TokenStream stream = new TokenStream(tokens);
        ExpressionNode result = parseExpression(stream, 0);
        ensureNoTrailingTokens(stream);
        return result;
    }

    public ExpressionNode parseExpression(TokenStream stream, int precedence) {
        Token token = stream.consume();
        TokenParser prefixParser = grammar.getParser(token.type());

        if (prefixParser == null) {
            String message = String.format("Could not parse \"%s\". It cannot be used in this position.", token.value());
            throw new ExpressionParseException(INVALID_TOKEN_PLACEMENT, message, token.position());
        }

        ExpressionNode left = prefixParser.parsePrefix(this, stream, token);

        while (stream.hasMore()) {
            Token nextToken = stream.peek();
            TokenParser infixParser = grammar.getParser(nextToken.type());
            int nextPrecedence = (infixParser != null) ? infixParser.getPrecedence() : 0;

            if (precedence >= nextPrecedence) {
                break;
            }

            stream.consume();
            left = infixParser.parseInfix(this, stream, left, nextToken);
        }
        return left;
    }

    public FunctionCall parseFunctionCall(TokenStream stream, ExpressionNode left) {
        String functionName;
        int position;

        if (left instanceof IdentifierNode(String name, int position1)) {
            functionName = name;
            position = position1;
        } else if (left instanceof FunctionCall call) {
            functionName = call.name();
            position = call.position();
        } else {
            Token t = stream.peek();
            int errorPos = (t != null) ? t.position() : -1;
            throw new ExpressionParseException(INVALID_FUNCTION_TARGET, "Expected a function name before '('.", errorPos);
        }

        List<ExpressionNode> args = parseFunctionArguments(stream);
        return new FunctionCall(functionName, position, args);
    }

    private List<ExpressionNode> parseFunctionArguments(TokenStream stream) {
        List<ExpressionNode> args = new ArrayList<>();
        if (!stream.peek(TokenType.RIGHT_PAREN)) {
            do {
                args.add(parseExpression(stream, 0));
            } while (stream.match(TokenType.COMMA));
        }
        stream.consume(TokenType.RIGHT_PAREN);
        return args;
    }

    private void ensureNoTrailingTokens(TokenStream stream) {
        if (stream.hasMore()) {
            Token t = stream.peek();
            int pos = (t != null) ? t.position() : -1;
            throw new ExpressionParseException(TRAILING_CHARACTERS, "Unexpected extra characters at the end of expression", pos);
        }
    }
}
