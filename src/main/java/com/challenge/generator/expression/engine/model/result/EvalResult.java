package com.challenge.generator.expression.engine.model.result;

import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.TypeUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A sealed wrapper for evaluation results, distinguishing between typed single values,
 * multiple values, and an empty result. This modern implementation uses pattern
 * matching for conciseness and clarity.
 */
public sealed interface EvalResult permits StringValue, NumberValue, MultiValue, MapValue, EmptyValue, BooleanValue, DateTimeValue {

    EvalResult EMPTY = new EmptyValue();

    default String firstOrNull() {
        return switch (this) {
            case StringValue(String value) -> value;
            case NumberValue(java.math.BigDecimal value) -> value == null ? null : value.toPlainString();
            case MultiValue(List<String> values) -> values.isEmpty() ? null : values.getFirst();
            case MapValue(Map<String, String> values) -> values.isEmpty() ? null : values.values().iterator().next();
            case EmptyValue ignored -> null;
            case BooleanValue(boolean value) -> Boolean.toString(value);
            case DateTimeValue dt -> dt.firstOrNull();
        };
    }

    default List<String> asList() {
        return switch (this) {
            case StringValue(String value) -> value == null ? List.of() : List.of(value);
            case NumberValue(java.math.BigDecimal value) -> value == null ? List.of() : List.of(value.toPlainString());
            case MultiValue(List<String> values) -> Collections.unmodifiableList(values);
            case MapValue(Map<String, String> values) -> List.copyOf(values.values());
            case EmptyValue ignored -> List.of();
            case BooleanValue(boolean value) -> List.of(Boolean.toString(value));
            case DateTimeValue dt -> List.of(dt.firstOrNull());
        };
    }

    default DataType dataType() {
        return switch (this) {
            case StringValue(String ignored1) -> DataType.STRING;
            case NumberValue(java.math.BigDecimal value) ->
                    value != null && value.stripTrailingZeros().scale() <= 0 ? DataType.INTEGER : DataType.DECIMAL;
            case MultiValue(List<String> values) ->
                    values.isEmpty() ? DataType.UNKNOWN : TypeUtils.inferType(values.getFirst());
            case MapValue(Map<String, String> ignored1) -> DataType.MAP;
            case EmptyValue ignored -> DataType.UNKNOWN;
            case BooleanValue(boolean ignored) -> DataType.BOOLEAN;
            case DateTimeValue ignored -> DataType.DATETIME;
        };
    }
}
