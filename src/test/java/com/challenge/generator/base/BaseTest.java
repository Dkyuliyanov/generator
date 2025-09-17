package com.challenge.generator.base;

import com.challenge.generator.config.TestApplicationContext;
import com.challenge.generator.expression.api.dto.EmailListResponse;
import com.challenge.generator.expression.engine.core.ExpressionEvaluator;
import com.challenge.generator.expression.engine.core.ExpressionInterpreter;
import com.challenge.generator.expression.engine.core.FunctionRegistry;
import com.challenge.generator.expression.engine.function.provider.EqualsProvider;
import com.challenge.generator.expression.engine.parser.ExpressionParser;
import com.challenge.generator.expression.engine.parser.grammar.Grammar;
import com.challenge.generator.expression.engine.parser.Tokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unified base test class that provides all necessary configurations and dependencies
 * for unit tests, integration tests, and E2E tests.
 * This class combines functionality from BaseUnitTest, BaseIntegrationTest, and BaseE2ETest:
 * - Loads TestApplicationContext to provide all testing beans
 * - Provides all beans used throughout tests to avoid individual @Autowired annotations
 * - Provides parser-related dependencies (Grammar, Tokenizer) for unit tests
 * - Provides web testing capabilities (TestRestTemplate, port) for integration/E2E tests
 * - Provides AutoCloseable executor functionality for multithreading tests
 * - Includes helper methods for HTTP testing
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestApplicationContext.class)
public abstract class BaseTest {


    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;


    @Autowired
    protected Grammar grammar;

    @Autowired
    protected Tokenizer tokenizer;


    @Autowired
    protected ExpressionEvaluator expressionEvaluator;

    @Autowired
    protected FunctionRegistry functionRegistry;

    @Autowired
    protected EqualsProvider equalsProvider;

    @Autowired
    protected ExpressionInterpreter expressionInterpreter;



    /**
     * Creates an ExpressionParser instance using the autowired dependencies.
     * From BaseUnitTest.
     */
    protected ExpressionParser parser() {
        return new ExpressionParser(tokenizer, grammar);
    }

    /**
     * Creates an AutoCloseableExecutor for multithreading tests.
     * Moved from AutoCloseableExecutor class to centralize test utilities.
     */
    protected AutoCloseableExecutor createFixedThreadPool(int nThreads) {
        return new AutoCloseableExecutor(Executors.newFixedThreadPool(nThreads));
    }

    /**
     * AutoCloseable wrapper for ExecutorService to ensure proper cleanup in tests.
     * Moved from separate AutoCloseableExecutor class.
     */
    public record AutoCloseableExecutor(ExecutorService executorService) implements AutoCloseable {
        
        public ExecutorService executor() {
            return executorService;
        }

        @Override
        public void close() {
            executorService.shutdownNow();
        }
    }

    /**
     * Builds a URI for expression generation with parameters.
     * From BaseE2ETest.
     */
    protected URI buildUri(String expression, Map<String, ?> params) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/generate")
                .queryParam("expression", expression);
        params.forEach(builder::queryParam);
        return builder.build().encode().toUri();
    }

    /**
     * Performs a GET request and asserts successful response.
     * From BaseE2ETest.
     */
    protected <T> T getOk(URI uri) {
        ResponseEntity<T> response = restTemplate.getForEntity(uri, (Class<T>) EmailListResponse.class);
        assertNotNull(response);
        assertTrue(response.getStatusCode().is2xxSuccessful(), "Expected 2xx status");
        T body = response.getBody();
        assertNotNull(body, "Response body should not be null");
        return body;
    }

    /**
     * Performs a GET request and asserts bad request response.
     * From BaseE2ETest.
     */
    protected Map<?,?> getBadRequest(URI uri) {
        ResponseEntity<Map> response = restTemplate.getForEntity(uri, Map.class);
        assertNotNull(response);
        assertEquals(400, response.getStatusCode().value());
        Map<?,?> body = response.getBody();
        assertNotNull(body);
        return body;
    }
}
