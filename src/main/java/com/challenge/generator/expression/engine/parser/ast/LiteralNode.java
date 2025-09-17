package com.challenge.generator.expression.engine.parser.ast;

import com.challenge.generator.expression.engine.model.DataType;

/**
 * AST node for a typed literal value.
 */
public record LiteralNode(Object value, DataType type, int position) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
