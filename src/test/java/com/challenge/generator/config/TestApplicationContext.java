package com.challenge.generator.config;

import com.challenge.generator.expression.engine.core.ExpressionInterpreter;
import com.challenge.generator.expression.engine.core.FunctionRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Test configuration that provides additional beans needed for testing.
 * This eliminates the need for manual bean creation in test setup methods.
 */
@TestConfiguration
public class TestApplicationContext {

    /**
     * Provides ExpressionInterpreter as a Spring bean for testing.
     * Uses the autowired FunctionRegistry which includes all registered function providers.
     */
    @Bean
    public ExpressionInterpreter expressionInterpreter(FunctionRegistry functionRegistry) {
        return new ExpressionInterpreter(functionRegistry);
    }
}
