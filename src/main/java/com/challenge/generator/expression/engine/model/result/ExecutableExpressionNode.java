package com.challenge.generator.expression.engine.model.result;

import com.challenge.generator.expression.engine.model.EvaluationContext;

@FunctionalInterface
public interface ExecutableExpressionNode {
    EvalResult evaluate(EvaluationContext inputs);
}
