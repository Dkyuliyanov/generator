package com.challenge.generator.expression.engine.parser.ast;

import java.util.Map;

/**
 * AST node representing a map literal with key-value pairs.
 */
public record MapLiteralNode(Map<String, String> entries, int position) implements ExpressionNode {
    @Override
    public <R> R accept(NodeVisitor<R> visitor) {
        return visitor.visit(this);
    }
}
