# Adding New Functions - Quick Guide

Simple 3-step process to add new functions to the expression engine.

## Steps

### 1. Create Provider Class

Create `src/main/java/com/challenge/generator/expression/engine/function/provider/YourFunctionProvider.java`:

```java
package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class YourFunctionProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.YOUR_FUNCTION.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        requireArgCount(arguments, 1, getFunctionName());
        ExecutableExpressionNode inputExpr = arguments.get(0);

        return inputs -> {
            String input = inputExpr.evaluate(inputs).firstOrNull();
            if (input == null || input.isEmpty()) {
                return EvalResult.EMPTY;
            }
            return new StringValue(input.toUpperCase()); // Your logic here
        };
    }
}
```

### 2. Add to FunctionName Enum

Update `src/main/java/com/challenge/generator/expression/engine/function/FunctionName.java`:

```java
public enum FunctionName {
    // existing functions...
    YOUR_FUNCTION("yourfunction"),
    // ...
}
```

### 3. Create Test

Create `src/test/java/com/challenge/generator/unit/function/YourFunctionProviderTest.java`:

```java
package com.challenge.generator.unit.function;

import com.challenge.generator.expression.engine.function.provider.YourFunctionProvider;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import com.challenge.generator.expression.engine.model.result.StringValue;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class YourFunctionProviderTest {

    private final YourFunctionProvider provider = new YourFunctionProvider();
    private final EvaluationContext emptyContext = EvaluationContext.from(Map.of());

    @Test
    void shouldProcessInput() {
        ExecutableExpressionNode function = provider.create(List.of(s -> new StringValue("hello")));
        assertEquals("HELLO", function.evaluate(emptyContext).firstOrNull());
    }

    @Test
    void shouldHandleEmptyInput() {
        ExecutableExpressionNode function = provider.create(List.of(s -> new StringValue("")));
        assertEquals(0, function.evaluate(emptyContext).asList().size());
    }
}
```

## Examples

**FirstLetter Function:**

```java
// Replace the logic in step 1 with:
return new StringValue(input.substring(0, 1));
```

**LastLetter Function:**

```java
// Replace the logic in step 1 with:
return new StringValue(input.substring(input.length() -1));
```

## Ready!

Run tests: `./gradlew test --tests "YourFunctionProviderTest"`

Your function is automatically available in expressions once these 3 steps are complete.