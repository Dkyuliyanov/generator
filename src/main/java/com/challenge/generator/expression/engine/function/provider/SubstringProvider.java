package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubstringProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.SUBSTRING_OF.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        requireArgCount(arguments, 3, getFunctionName());
        ExecutableExpressionNode inputExpr = arguments.get(0);
        ExecutableExpressionNode startExpr = arguments.get(1);
        ExecutableExpressionNode endExpr = arguments.get(2);

        return inputs -> {
            String input = inputExpr.evaluate(inputs).firstOrNull();
            if (input == null || input.isEmpty()) {
                return EvalResult.EMPTY;
            }
            String startStr = startExpr.evaluate(inputs).firstOrNull();
            String endStr = endExpr.evaluate(inputs).firstOrNull();

            return createSubstring(input, startStr, endStr);
        };
    }

    private StringValue createSubstring(String input, String startStr, String endStr) {
        int startInclusive = Integer.parseInt(startStr);
        int endInclusive = Integer.parseInt(endStr);

        int len = input.length();
        int start = Math.max(1, startInclusive);
        int to = Math.min(endInclusive, len);

        if (to < start) {
            return new StringValue("");
        }
        int from = start - 1;
        String result = input.substring(from, to);
        return new StringValue(result);
    }
}
