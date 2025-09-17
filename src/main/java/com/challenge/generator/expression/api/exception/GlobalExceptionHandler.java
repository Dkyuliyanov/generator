package com.challenge.generator.expression.api.exception;

import com.challenge.generator.expression.engine.core.FunctionRegistry;
import com.challenge.generator.expression.engine.exception.EvaluationException;
import com.challenge.generator.expression.engine.exception.ExpressionParseException;
import com.challenge.generator.expression.engine.exception.InvalidArgumentCountException;
import com.challenge.generator.expression.engine.exception.UnknownFunctionException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.similarity.LevenshteinDistance;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {

    private final FunctionRegistry functionRegistry;
    private final LevenshteinDistance levenshtein = LevenshteinDistance.getDefaultInstance();


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        String message = ex.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        var body = createErrorBody(message, request, -1);
        log.warn("Constraint violation: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, -1);
        log.warn("Validation error: {} | expression='{}'", ex.getMessage(), request.getParameter("expression"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }


    @ExceptionHandler(UnknownFunctionException.class)
    public ResponseEntity<Map<String, Object>> handleUnknownFunction(UnknownFunctionException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, ex.getPosition());
        body.put("functionName", ex.getFunctionName());
        buildSuggestionFor(ex.getFunctionName()).ifPresent(suggestion -> body.put("suggestion", suggestion));
        log.warn("Unknown function error: {} | expression='{}' | position={}", ex.getMessage(), request.getParameter("expression"), ex.getPosition());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(InvalidArgumentCountException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidArgumentCount(InvalidArgumentCountException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, 0);
        body.put("functionName", ex.getFunctionName());
        body.put("expectedArgs", ex.getExpected());
        body.put("actualArgs", ex.getActual());
        log.warn("Invalid argument count: {} | expression='{}'", ex.getMessage(), request.getParameter("expression"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ExpressionParseException.class)
    public ResponseEntity<Map<String, Object>> handleExpressionParseException(ExpressionParseException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, ex.getPosition());
        body.put("errorCode", ex.getErrorCode());
        log.warn("ExpressionParseException: {} | errorCode={} | expression='{}' | position={}",
                ex.getMessage(), ex.getErrorCode(), request.getParameter("expression"), ex.getPosition());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(EvaluationException.class)
    public ResponseEntity<Map<String, Object>> handleGenericEvaluationException(EvaluationException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, -1);
        log.warn("Evaluation error: {} | expression='{}'", ex.getMessage(), request.getParameter("expression"));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<Map<String, Object>> handleApiException(ApiException ex, HttpServletRequest request) {
        var body = createErrorBody(ex.getMessage(), request, 0);
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleOther(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error while handling request: {}", ex.getMessage(), ex);
        var body = createErrorBody("Internal server error", request, -1);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }


    private Map<String, Object> createErrorBody(String message, HttpServletRequest request, int position) {
        var body = new LinkedHashMap<String, Object>();
        body.put("error", cleanMessage(message));
        body.put("expression", request.getParameter("expression"));
        if (position >= 0) {
            body.put("position", position);
        }
        return body;
    }

    private Optional<String> buildSuggestionFor(String unknownFunctionName) {
        if (unknownFunctionName == null) return Optional.empty();

        return functionRegistry.getRegisteredFunctionNames().stream()
                .min(Comparator.comparingInt(candidate -> levenshtein.apply(unknownFunctionName, candidate)))
                .filter(bestMatch -> levenshtein.apply(unknownFunctionName, bestMatch) <= Math.max(1, unknownFunctionName.length() / 3))
                .map(bestMatch -> "Did you mean '" + bestMatch + "'?");
    }

    private String cleanMessage(String msg) {
        if (msg == null) return null;
        return msg.replaceAll("^[a-zA-Z0-9_$.]*Exception: ", "");
    }
}
