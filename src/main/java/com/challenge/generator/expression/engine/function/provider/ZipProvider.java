package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.ContractChecker;
import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.MultiValue;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class ZipProvider implements FunctionProvider {

    private record EvaluatedArgument(List<String> values, boolean isMulti) {
    }

    @Override
    public String getFunctionName() {
        return FunctionName.ZIP.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        ContractChecker.forFunction(getFunctionName(), arguments)
                .requireMinArgs(2)
                .requireArgCountLessThan(99);

        return inputs -> {
            List<EvaluatedArgument> evaluatedArgs = evaluateArguments(arguments, inputs);
            List<EvaluatedArgument> multiArgs = getMultiValueArgs(evaluatedArgs);

            if (multiArgs.isEmpty()) {
                return new StringValue(concatenateScalarArgs(evaluatedArgs));
            }

            if (multiArgs.stream().anyMatch(arg -> arg.values().isEmpty())) {
                return EvalResult.EMPTY;
            }

            int minSize = getMinSize(multiArgs);
            List<String> results = zip(evaluatedArgs, minSize);
            return results.isEmpty() ? EvalResult.EMPTY : new MultiValue(results);
        };
    }

    private List<EvaluatedArgument> evaluateArguments(List<ExecutableExpressionNode> arguments, EvaluationContext inputs) {
        return arguments.stream()
                .map(arg -> {
                    EvalResult res = arg.evaluate(inputs);
                    boolean isMulti = res instanceof MultiValue;
                    List<String> values = res.asList();
                    return new EvaluatedArgument(values, isMulti);
                })
                .collect(Collectors.toList());
    }

    private List<EvaluatedArgument> getMultiValueArgs(List<EvaluatedArgument> evaluatedArgs) {
        return evaluatedArgs.stream().filter(EvaluatedArgument::isMulti).collect(Collectors.toList());
    }

    private String concatenateScalarArgs(List<EvaluatedArgument> evaluatedArgs) {
        return evaluatedArgs.stream()
                .map(arg -> arg.values().isEmpty() ? "" : Objects.toString(arg.values().getFirst(), ""))
                .collect(Collectors.joining());
    }

    private int getMinSize(List<EvaluatedArgument> multiArgs) {
        return multiArgs.stream()
                .mapToInt(arg -> arg.values().size())
                .min()
                .orElse(0);
    }

    private List<String> zip(List<EvaluatedArgument> allArgs, int size) {
        return IntStream.range(0, size)
                .mapToObj(i -> allArgs.stream()
                        .map(arg -> {
                            if (arg.isMulti()) {
                                return Objects.toString(arg.values().get(i), "");
                            }
                            return arg.values().isEmpty() ? "" : Objects.toString(arg.values().getFirst(), "");
                        })
                        .collect(Collectors.joining()))
                .collect(Collectors.toList());
    }
}
