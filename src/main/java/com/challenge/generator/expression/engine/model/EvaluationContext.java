package com.challenge.generator.expression.engine.model;

import java.util.Map;

public record EvaluationContext(Map<String, Object> data) {

    public EvaluationContext(Map<String, Object> data) {
        this.data = data == null ? Map.of() : data;
    }

    public static EvaluationContext from(Map<String, Object> data) {
        return new EvaluationContext(data);
    }

}
