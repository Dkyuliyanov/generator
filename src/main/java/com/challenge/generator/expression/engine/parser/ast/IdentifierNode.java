package com.challenge.generator.expression.engine.parser.ast;

/**
 * AST node for an identifier reference (variable or function name in call context).
 */
public record IdentifierNode(String name, int position) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
