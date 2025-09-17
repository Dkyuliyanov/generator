package com.challenge.generator.expression.engine.model.result;

import java.util.Map;

/** Map values representing key-value pairs. */
public record MapValue(Map<String, String> values) implements EvalResult {}
