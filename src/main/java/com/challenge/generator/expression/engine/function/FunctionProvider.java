package com.challenge.generator.expression.engine.function;

import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;

import java.util.List;

public interface FunctionProvider {
    String getFunctionName();

    ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments);

    default void requireArgCount(List<ExecutableExpressionNode> arguments, int expected, String functionName) {
        ContractChecker.forFunction(functionName, arguments).requireExactArgs(expected);
    }
}
