package com.challenge.generator.expression.engine.parser.ast;

import java.util.List;

/**
 * AST node representing a function call.
 */
public record FunctionCall(String name, int position, List<ExpressionNode> arguments) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
