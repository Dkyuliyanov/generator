package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.MultiValue;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class SplitProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.SPLIT.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        requireArgCount(arguments, 2, getFunctionName());
        ExecutableExpressionNode inputExpr = arguments.get(0);
        ExecutableExpressionNode delimiterExpr = arguments.get(1);

        return inputs -> {
            String input = inputExpr.evaluate(inputs).firstOrNull();
            if (input == null || input.isEmpty()) {
                return EvalResult.EMPTY;
            }

            String delimiter = Objects.toString(delimiterExpr.evaluate(inputs).firstOrNull(), "");

            if (delimiter.isEmpty()) {
                return splitByCharacter(input);
            }

            String quoted = Pattern.quote(delimiter);
            return new MultiValue(Arrays.asList(input.split(quoted, -1)));
        };
    }

    private EvalResult splitByCharacter(String input) {
        List<String> chars = input.codePoints()
                .mapToObj(cp -> new String(Character.toChars(cp)))
                .collect(Collectors.toList());
        return chars.isEmpty() ? EvalResult.EMPTY : new MultiValue(chars);
    }
}
