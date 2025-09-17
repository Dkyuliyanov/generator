package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.result.BooleanValue;
import com.challenge.generator.expression.engine.model.result.DateTimeValue;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.MapValue;
import com.challenge.generator.expression.engine.model.result.MultiValue;
import com.challenge.generator.expression.engine.model.result.NumberValue;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class EqualsProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.EQUALS.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        requireArgCount(arguments, 2, getFunctionName());
        ExecutableExpressionNode left = arguments.get(0);
        ExecutableExpressionNode right = arguments.get(1);

        return inputs -> {
            EvalResult leftResult = left.evaluate(inputs);
            EvalResult rightResult = right.evaluate(inputs);

            Object leftValue = extractValue(leftResult);
            Object rightValue = extractValue(rightResult);

            return new BooleanValue(Objects.equals(leftValue, rightValue));
        };
    }

    private Object extractValue(EvalResult result) {
        return switch (result) {
            case NumberValue nv -> nv.value();
            case StringValue sv -> sv.value();
            case BooleanValue bv -> bv.value();
            case DateTimeValue dt -> dt.firstOrNull();
            case MapValue mv -> mv.values();
            case MultiValue mv -> mv.values();
            default -> null;
        };
    }
}
