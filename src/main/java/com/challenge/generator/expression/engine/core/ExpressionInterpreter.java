package com.challenge.generator.expression.engine.core;

import com.challenge.generator.expression.engine.exception.InvalidConditionTypeException;
import com.challenge.generator.expression.engine.exception.UnknownFunctionException;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.*;
import com.challenge.generator.expression.engine.parser.ast.*;
import com.challenge.generator.expression.engine.parser.token.Operator;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.List;

/**
 * Walks the AST (ExpressionNode) and evaluates it to an EvalResult.
 */
@Slf4j
public class ExpressionInterpreter implements NodeVisitor<EvalResult> {

    private final FunctionRegistry functionRegistry;

    public ExpressionInterpreter(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }

    private final ThreadLocal<EvaluationContext> currentContext = new ThreadLocal<>();

    public EvalResult evaluate(ExpressionNode node, EvaluationContext inputs) {
        currentContext.set(inputs);
        try {
            var result = node.accept(this);
            log.atDebug().log("evaluate end: node={}, resultType={}", node.getClass().getSimpleName(), result.getClass().getSimpleName());
            return result;
        } finally {
            currentContext.remove();
        }
    }

    @Override
    public EvalResult visit(LiteralNode lit) {
        var result = switch (lit.type()) {
            case STRING -> new StringValue((String) lit.value());
            case INTEGER, DECIMAL -> new NumberValue((BigDecimal) lit.value());
            default -> new StringValue(lit.value() == null ? null : lit.value().toString());
        };
        log.atDebug().log("literal evaluated: resultType={}", result.getClass().getSimpleName());
        return result;
    }

    @Override
    public EvalResult visit(IdentifierNode id) {
        Object raw = currentContext.get().data().get(id.name());
        var result = toValueFromIdentifier(raw);
        log.atDebug().log("identifier resolved: name={}, rawType={}, resultType={}", id.name(), raw == null ? "null" : raw.getClass().getSimpleName(), result.getClass().getSimpleName());
        return result;
    }

    @Override
    public EvalResult visit(MapLiteralNode map) {
        var result = new MapValue(map.entries());
        log.atDebug().log("map literal evaluated: entries={}", map.entries().size());
        return result;
    }

    @Override
    public EvalResult visit(FunctionCall call) {
        return evaluateFunction(call, currentContext.get());
    }

    @Override
    public EvalResult visit(ConditionalNode cond) {
        return evaluateConditional(cond, currentContext.get());
    }

    @Override
    public EvalResult visit(BinaryOpNode bin) {
        return evaluateBinary(bin, currentContext.get());
    }

    private EvalResult evaluateFunction(FunctionCall call, EvaluationContext inputs) {
        String name = call.name();
        int position = call.position();
        List<ExpressionNode> arguments = call.arguments();
        log.atDebug().log("function call: name={}, argCount={}", name, arguments.size());
        var provider = functionRegistry.get(name)
                .orElseThrow(() -> new UnknownFunctionException(name, position));
        var args = arguments.stream()
                .map(arg -> (ExecutableExpressionNode) in -> evaluate(arg, in))
                .toList();
        var exec = provider.create(args);
        var out = exec.evaluate(inputs);
        log.atDebug().log("function result: name={}, resultType={}", name, out.getClass().getSimpleName());
        return out;
    }

    private EvalResult evaluateConditional(ConditionalNode node, EvaluationContext inputs) {
        var condResult = evaluate(node.condition(), inputs);
        if (condResult instanceof BooleanValue(boolean value)) {
            return value ? evaluate(node.thenBranch(), inputs) : evaluate(node.elseBranch(), inputs);
        }
        throw new InvalidConditionTypeException(
                "Conditional expression must evaluate to a boolean. Use functions like equals(...) to produce a boolean result.");
    }

    private EvalResult evaluateBinary(BinaryOpNode node, EvaluationContext inputs) {
        var leftResult = evaluate(node.left(), inputs);
        var rightResult = evaluate(node.right(), inputs);
        var op = Operator.fromSymbol(node.operator());
        log.atDebug().log("binary eval: op={}, leftType={}, rightType={}", op, leftResult.getClass().getSimpleName(), rightResult.getClass().getSimpleName());
        if (op == Operator.CONCATENATE) {
            return handleConcatenation(leftResult, rightResult);
        }
        if (isComparisonOperator(op)) {
            return evaluateComparison(op, leftResult, rightResult);
        }
        var leftNumCoerced = coerceToNumber(leftResult);
        var rightNumCoerced = coerceToNumber(rightResult);
        if (leftNumCoerced != null && rightNumCoerced != null) {
            return performArithmetic(op, leftNumCoerced, rightNumCoerced);
        }
        throw new UnsupportedOperationException("Operator not supported for non-numeric types: " + op);
    }

    private EvalResult toValueFromIdentifier(Object raw) {
        return switch (raw) {
            case null -> new StringValue("");
            case BigDecimal bd -> new NumberValue(bd);
            case Number num -> new NumberValue(new BigDecimal(String.valueOf(num)));
            case Boolean b -> new BooleanValue(b);
            case java.util.Map<?, ?> map -> {
                var m = new java.util.LinkedHashMap<String, String>();
                for (var e : map.entrySet()) {
                    m.put(String.valueOf(e.getKey()), String.valueOf(e.getValue()));
                }
                yield new MapValue(m);
            }
            case java.util.List<?> list -> {
                var values = list.stream().map(String::valueOf).toList();
                yield new MultiValue(values);
            }
            default -> new StringValue(String.valueOf(raw));
        };
    }

    private EvalResult handleConcatenation(EvalResult leftResult, EvalResult rightResult) {
        log.atDebug().log("concat operands: leftType={}, rightType={}", leftResult.getClass().getSimpleName(), rightResult.getClass().getSimpleName());
        if (leftResult instanceof EmptyValue || rightResult instanceof EmptyValue) {
            log.atDebug().log("concat short-circuit: EMPTY operand");
            return EvalResult.EMPTY;
        }
        boolean involvesList = (leftResult instanceof MultiValue) || (rightResult instanceof MultiValue);
        if (involvesList && (leftResult.asList().isEmpty() || rightResult.asList().isEmpty())) {
            log.atDebug().log("concat short-circuit: empty list operand");
            return EvalResult.EMPTY;
        }
        var res = new StringValue(safeFirst(leftResult) + safeFirst(rightResult));
        log.atDebug().log("concat result: {}", res.firstOrNull());
        return res;
    }

    private EvalResult performArithmetic(Operator op, NumberValue leftNum, NumberValue rightNum) {
        BigDecimal l = leftNum.value();
        BigDecimal r = rightNum.value();
        log.atDebug().log("arithmetic: op={}, left={}, right={}", op, l, r);
        return switch (op) {
            case PLUS -> new NumberValue(l.add(r));
            case MINUS -> new NumberValue(l.subtract(r));
            case MULTIPLY -> new NumberValue(l.multiply(r));
            case DIVIDE -> {
                if (r.compareTo(BigDecimal.ZERO) == 0) throw new ArithmeticException("Division by zero");
                var lScale = l.stripTrailingZeros().scale();
                var rScale = r.stripTrailingZeros().scale();
                if (lScale <= 0 && rScale <= 0) {
                    yield new NumberValue(l.divideToIntegralValue(r));
                }
                yield new NumberValue(l.divide(r, MathContext.DECIMAL64));
            }
            default -> throw new UnsupportedOperationException("Operator not supported for numbers: " + op.getSymbol());
        };
    }

    private String safeFirst(EvalResult result) {
        var s = result.firstOrNull();
        return s == null ? "" : s;
    }

    private boolean isComparisonOperator(Operator op) {
        return switch (op) {
            case LESS_THAN, GREATER_THAN, EQUALS, LESS_THAN_OR_EQUAL, GREATER_THAN_OR_EQUAL, NOT_EQUALS -> true;
            default -> false;
        };
    }

    private EvalResult evaluateComparison(Operator op, EvalResult leftResult, EvalResult rightResult) {
        log.atDebug().log("comparison eval: op={}, leftType={}, rightType={}", op, leftResult.getClass().getSimpleName(), rightResult.getClass().getSimpleName());
        if (leftResult.asList().isEmpty() || rightResult.asList().isEmpty()) {
            var result = switch (op) {
                case EQUALS -> leftResult.asList().isEmpty() && rightResult.asList().isEmpty();
                case NOT_EQUALS -> !(leftResult.asList().isEmpty() && rightResult.asList().isEmpty());
                default -> false;
            };
            log.atDebug().log("comparison on empties result: {}", result);
            return new BooleanValue(result);
        }
        var lnCoerced = coerceToNumber(leftResult);
        var rnCoerced = coerceToNumber(rightResult);
        if (lnCoerced != null && rnCoerced != null) {
            boolean res = comparisonResult(op, lnCoerced, rnCoerced);
            log.atDebug().log("numeric comparison result: {} vs {} -> {}", lnCoerced.value(), rnCoerced.value(), res);
            return new BooleanValue(res);
        }
        if (leftResult instanceof BooleanValue(boolean value) && rightResult instanceof BooleanValue(boolean value1)) {
            boolean res = switch (op) {
                case EQUALS -> value == value1;
                case NOT_EQUALS -> value != value1;
                default ->
                        throw new UnsupportedOperationException("Unsupported comparison operator for booleans: " + op);
            };
            log.atDebug().log("boolean comparison result: {} vs {} -> {}", value, value1, res);
            return new BooleanValue(res);
        }
        String leftStr = leftResult.firstOrNull();
        String rightStr = rightResult.firstOrNull();
        String left = leftStr == null ? "" : leftStr;
        String right = rightStr == null ? "" : rightStr;
        boolean res = switch (op) {
            case LESS_THAN -> left.compareTo(right) < 0;
            case GREATER_THAN -> left.compareTo(right) > 0;
            case EQUALS -> left.equals(right);
            case LESS_THAN_OR_EQUAL -> left.compareTo(right) <= 0;
            case GREATER_THAN_OR_EQUAL -> left.compareTo(right) >= 0;
            case NOT_EQUALS -> !left.equals(right);
            default -> throw new UnsupportedOperationException("Unsupported comparison operator: " + op);
        };
        log.atDebug().log("string comparison result: '{}' vs '{}' -> {}", left, right, res);
        return new BooleanValue(res);
    }

    private boolean comparisonResult(Operator op, NumberValue lnCoerced, NumberValue rnCoerced) {
        int cmp = lnCoerced.value().compareTo(rnCoerced.value());
        return switch (op) {
            case LESS_THAN -> cmp < 0;
            case GREATER_THAN -> cmp > 0;
            case EQUALS -> cmp == 0;
            case LESS_THAN_OR_EQUAL -> cmp <= 0;
            case GREATER_THAN_OR_EQUAL -> cmp >= 0;
            case NOT_EQUALS -> cmp != 0;
            default -> throw new UnsupportedOperationException("Unsupported comparison operator: " + op);
        };
    }

    private NumberValue coerceToNumber(EvalResult result) {
        if (result instanceof NumberValue n) return n;
        String s = result.firstOrNull();
        if (s == null) return null;
        if (com.challenge.generator.expression.engine.model.TypeUtils.isInteger(s) ||
                com.challenge.generator.expression.engine.model.TypeUtils.isDecimal(s)) {
            var n = new NumberValue(new java.math.BigDecimal(s));
            log.atDebug().log("coerced to number: {}", n.value());
            return n;
        }
        return null;
    }

}
