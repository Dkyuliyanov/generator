package com.challenge.generator.expression.api.filter;

import com.challenge.generator.expression.api.service.ExpressionPreprocessor;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class ExpressionRequestInterceptor implements Filter {

    private static final String GENERATE_ENDPOINT_URI = "/generate";
    private static final String EXPRESSION_PARAM = "expression";

    private final ExpressionPreprocessor expressionPreprocessor;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (shouldPreprocess(request)) {
            log.debug("Intercepting GET request for expression preprocessing.");
            var wrappedRequest = new PreprocessingRequestWrapper((HttpServletRequest) request, expressionPreprocessor);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }

    private boolean shouldPreprocess(ServletRequest request) {
        if (!(request instanceof HttpServletRequest httpRequest)) {
            return false;
        }
        return "GET".equalsIgnoreCase(httpRequest.getMethod()) &&
                httpRequest.getRequestURI().endsWith(GENERATE_ENDPOINT_URI);
    }

    private static class PreprocessingRequestWrapper extends HttpServletRequestWrapper {

        private static final Pattern PARAM_DELIMITER = Pattern.compile("&(?=[a-zA-Z][a-zA-Z0-9]*=)");

        private final ExpressionPreprocessor preprocessor;
        private final Map<String, String[]> processedParameters;
        private boolean parametersProcessed = false;

        public PreprocessingRequestWrapper(HttpServletRequest request, ExpressionPreprocessor preprocessor) {
            super(request);
            this.preprocessor = preprocessor;
            this.processedParameters = new HashMap<>();
        }

        @Override
        public String getParameter(String name) {
            processParametersIfNeeded();
            String[] values = processedParameters.get(name);
            return (values != null && values.length > 0) ? values[0] : null;
        }

        @Override
        public String[] getParameterValues(String name) {
            processParametersIfNeeded();
            return processedParameters.get(name);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            processParametersIfNeeded();
            return Collections.unmodifiableMap(processedParameters);
        }

        private void processParametersIfNeeded() {
            if (parametersProcessed) {
                return;
            }

            String queryString = getQueryString();
            if (StringUtils.hasText(queryString)) {
                log.debug("Processing raw query string: {}", queryString);
                parseAndPreprocessParameters(queryString);
            }

            parametersProcessed = true;
        }

        private void parseAndPreprocessParameters(String queryString) {
            PARAM_DELIMITER.splitAsStream(queryString)
                    .forEach(this::processParameterPair);
            log.debug("Processed {} parameters.", processedParameters.size());
        }

        private void processParameterPair(String pair) {
            int equalsIndex = pair.indexOf('=');
            if (equalsIndex <= 0) {
                return;
            }

            String key = decode(pair.substring(0, equalsIndex));
            String value = decode(pair.substring(equalsIndex + 1));

            String finalValue = maybePreprocessValue(key, value);
            processedParameters.put(key, new String[]{finalValue});
        }

        private String maybePreprocessValue(String key, String originalValue) {
            if (EXPRESSION_PARAM.equals(key)) {
                String preprocessedValue = preprocessor.preprocess(originalValue);
                log.debug("Preprocessed expression: '{}' -> '{}'", originalValue, preprocessedValue);
                return preprocessedValue;
            }
            return originalValue;
        }

        private String decode(String value) {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        }
    }
}
