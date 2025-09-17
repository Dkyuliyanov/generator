package com.challenge.generator.expression.engine.parser.ast;

/**
 * Marker interface for all AST nodes representing expressions.
 * This package is pure AST and must not depend on parsing or runtime evaluation.
 */
public interface ExpressionNode {
    <R> R accept(NodeVisitor<R> visitor);
}
