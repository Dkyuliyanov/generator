package com.challenge.generator.expression.api.dto;

public record CurlGenerationResponse(
        String curlRequest,
        String hint
) {
    public CurlGenerationResponse(String curlRequest) {
        this(curlRequest, null);
    }
}
