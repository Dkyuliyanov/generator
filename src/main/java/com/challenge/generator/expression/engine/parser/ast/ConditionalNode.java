package com.challenge.generator.expression.engine.parser.ast;

/**
 * AST node for ternary operator: condition ? thenBranch : elseBranch
 */
public record ConditionalNode(ExpressionNode condition, ExpressionNode thenBranch, ExpressionNode elseBranch) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
