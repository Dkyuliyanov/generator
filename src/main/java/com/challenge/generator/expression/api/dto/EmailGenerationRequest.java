package com.challenge.generator.expression.api.dto;

import java.util.Map;

public record EmailGenerationRequest(String expression, Map<String, Object> input) {
}
