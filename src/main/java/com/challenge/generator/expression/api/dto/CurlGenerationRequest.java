package com.challenge.generator.expression.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CurlGenerationRequest(
        @NotBlank(message = "Expression is required")
        String expression,
        
        @NotNull(message = "Inputs are required")
        Map<String, Object> inputs
) {
}
