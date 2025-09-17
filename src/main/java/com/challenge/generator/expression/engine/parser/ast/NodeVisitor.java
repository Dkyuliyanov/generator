package com.challenge.generator.expression.engine.parser.ast;

/**
 * Generic visitor interface for AST nodes.
 * @param <R> the return type of the visit operation
 */
public interface NodeVisitor<R> {
    R visit(LiteralNode node);
    R visit(IdentifierNode node);
    R visit(BinaryOpNode node);
    R visit(FunctionCall node);
    R visit(ConditionalNode node);
    R visit(MapLiteralNode node);
}
