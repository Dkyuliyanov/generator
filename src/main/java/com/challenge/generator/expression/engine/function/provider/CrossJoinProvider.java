package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.ContractChecker;
import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.MultiValue;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Component
public class CrossJoinProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.CROSS_JOIN.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        ContractChecker.forFunction(getFunctionName(), arguments)
                .requireMinArgs(2)
                .requireArgCountLessThan(99);

        return inputs -> {
            List<String> results = new ArrayList<>();
            results.add("");

            for (ExecutableExpressionNode arg : arguments) {
                List<String> argValues = arg.evaluate(inputs).asList();
                if (argValues.isEmpty()) {
                    return EvalResult.EMPTY;
                }
                results = results.stream()
                        .flatMap(prefix -> argValues.stream()
                                .map(val -> Objects.toString(prefix, "") + Objects.toString(val, "")))
                        .toList();
            }
            return new MultiValue(results);
        };
    }
}
