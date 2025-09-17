package com.challenge.generator.expression.engine.parser.ast;

/**
 * AST node for binary infix operations like +, -, etc.
 */
public record BinaryOpNode(String operator, ExpressionNode left, ExpressionNode right, int position) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
