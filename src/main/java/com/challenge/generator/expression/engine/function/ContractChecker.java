package com.challenge.generator.expression.engine.function;

import com.challenge.generator.expression.engine.exception.InvalidArgumentCountException;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import java.util.List;

public record ContractChecker(String functionName, List<ExecutableExpressionNode> arguments) {

    public static ContractChecker forFunction(String functionName, List<ExecutableExpressionNode> arguments) {
        return new ContractChecker(functionName, arguments);
    }

    public void requireExactArgs(int expected) {
        int actual = getActualArgumentCount();
        if (actual != expected) {
            throw new InvalidArgumentCountException(functionName, expected, actual);
        }
    }

    public ContractChecker requireMinArgs(int min) {
        int actual = getActualArgumentCount();
        if (actual < min) {
            throw new InvalidArgumentCountException(functionName, min, actual);
        }
        return this;
    }

    public void requireArgCountLessThan(int exclusiveUpperBound) {
        int actual = getActualArgumentCount();
        if (actual >= exclusiveUpperBound) {
            throw new InvalidArgumentCountException(functionName, exclusiveUpperBound - 1, actual);
        }
    }

    private int getActualArgumentCount() {
        return (arguments == null) ? 0 : arguments.size();
    }
}
