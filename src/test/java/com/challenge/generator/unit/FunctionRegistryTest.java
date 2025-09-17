package com.challenge.generator.unit;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.core.FunctionRegistry;
import com.challenge.generator.expression.engine.exception.DuplicateFunctionNameException;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.function.provider.SplitProvider;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("FunctionRegistry Tests")
class FunctionRegistryTest extends BaseTest {

    @Test
    @DisplayName("should throw an exception for duplicate function names")
    void shouldThrowForDuplicateFunctionNames() {
        FunctionProvider provider1 = new DummyProvider("duplicate");
        FunctionProvider provider2 = new DummyProvider("DUPLICATE");
        assertThrows(DuplicateFunctionNameException.class, () -> FunctionRegistry.fromProviders(List.of(provider1, provider2)));
    }

    @ParameterizedTest(name = "should be case-insensitive for function name: {0}")
    @ValueSource(strings = {"SPLIT", "split", "Split"})
    @DisplayName("should retrieve functions in a case-insensitive manner")
    void shouldBeCaseInsensitive(String functionName) {
        var provider = functionRegistry.get(functionName);
        assertTrue(provider.isPresent());
        assertInstanceOf(SplitProvider.class, provider.get());
    }

    private record DummyProvider(String name) implements FunctionProvider {

        @Override
            public String getFunctionName() {
                return name;
            }

            @Override
            public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
                return inputs -> new StringValue("dummy");
            }

            @Override
            public String toString() {
                return "DummyProvider{" + "name='" + name + '\'' + '}';
            }
        }
}
