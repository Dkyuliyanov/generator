package com.challenge.generator.expression.api.controller;

import com.challenge.generator.expression.api.exception.ValidationMessages;
import com.challenge.generator.expression.api.dto.EmailData;
import com.challenge.generator.expression.api.dto.EmailGenerationRequest;
import com.challenge.generator.expression.api.dto.EmailListResponse;
import com.challenge.generator.expression.api.dto.CurlGenerationRequest;
import com.challenge.generator.expression.api.dto.CurlGenerationResponse;
import com.challenge.generator.expression.api.exception.ValidationException;
import com.challenge.generator.expression.engine.core.ExpressionEvaluator;
import com.challenge.generator.expression.api.util.EmailValidator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
public class ExpressionController {

    private final ExpressionEvaluator expressionEvaluator;

    @GetMapping("generate")
    public EmailListResponse generateEmails(
            @RequestParam @NotBlank(message = ValidationMessages.EXPRESSION_REQUIRED) String expression,
            @RequestParam Map<String, String> inputs) {

        var dynamicInputs = new HashMap<String, Object>(inputs);
        dynamicInputs.remove("expression");
        if (dynamicInputs.isEmpty()) {
            throw new ValidationException(ValidationMessages.DYNAMIC_INPUT_REQUIRED);
        }

        var request = new EmailGenerationRequest(expression, dynamicInputs);

        log.info("Generating emails with expression:\n'{}' \nand inputs: \n{}", request.expression(), request.input());
        var results = expressionEvaluator.generateResults(request);

        var emailData = results.stream()
                .map(EmailData::from)
                .toList();
        var response = new EmailListResponse(emailData);
        log.info("Returning {} generated email address(es)", emailData.size());

        return response;
    }

    //TODO: only for testing purposes. The challenge outlined the need to use query params.
    @PostMapping("generate-curl")
    public CurlGenerationResponse generateCurl(@Valid @RequestBody CurlGenerationRequest request) {
        log.info("Generating curl request with expression: '{}' and inputs: {}", request.expression(), request.inputs().keySet());


        if (request.inputs() == null || request.inputs().isEmpty()) {
            throw new ValidationException(ValidationMessages.DYNAMIC_INPUT_REQUIRED);
        }
        

        var emailGenerationRequest = new EmailGenerationRequest(request.expression(), request.inputs());
        List<String> results = expressionEvaluator.generateResults(emailGenerationRequest);
        

        boolean containsInvalidEmails = EmailValidator.containsInvalidEmails(results);
        String hint = null;
        
        if (containsInvalidEmails) {
            List<String> invalidEmails = EmailValidator.getInvalidEmails(results);
            hint = String.format("Warning: This curl request will produce %d invalid email address(es): %s", 
                    invalidEmails.size(), String.join(", ", invalidEmails));
        }
        

        String curlRequest = generateCurlRequestString(request.expression(), request.inputs());
        
        log.info("Generated curl request with {} result(s), invalid emails: {}", results.size(), containsInvalidEmails);
        
        return new CurlGenerationResponse(curlRequest, hint);
    }
    
    private String generateCurlRequestString(String expression, Map<String, Object> inputs) {
        StringBuilder curlBuilder = new StringBuilder();
        curlBuilder.append("curl --location --request GET \\\n");
        curlBuilder.append("  'http://localhost:8081/generate?");
        

        curlBuilder.append("expression=").append(urlEncode(expression));
        

        for (Map.Entry<String, Object> entry : inputs.entrySet()) {
            curlBuilder.append("&").append(entry.getKey()).append("=").append(urlEncode(entry.getValue().toString()));
        }
        
        curlBuilder.append("'");
        
        return curlBuilder.toString();
    }
    
    private String urlEncode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
