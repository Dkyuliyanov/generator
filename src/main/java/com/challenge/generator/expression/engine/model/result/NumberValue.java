package com.challenge.generator.expression.engine.model.result;

import java.math.BigDecimal;

/** Numeric value backed by BigDecimal. */
public record NumberValue(BigDecimal value) implements EvalResult {}
