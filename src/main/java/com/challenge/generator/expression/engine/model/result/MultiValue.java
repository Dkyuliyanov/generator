package com.challenge.generator.expression.engine.model.result;

import java.util.List;

/** Multiple values. */
public record MultiValue(List<String> values) implements EvalResult {}
