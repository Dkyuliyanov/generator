package com.challenge.generator.expression.engine.core;

import com.challenge.generator.expression.engine.exception.DuplicateFunctionNameException;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Immutable, case-insensitive registry of expression functions.
 * Built at startup and fails fast on duplicate function names.
 */
@Slf4j
public record FunctionRegistry(Map<String, FunctionProvider> availableFunctions) {

    public static FunctionRegistry fromProviders(List<FunctionProvider> providers) {
        if (providers == null || providers.isEmpty()) {
            log.debug("No function providers found. Initializing an empty FunctionRegistry.");
            return new FunctionRegistry(Collections.emptyMap());
        }

        var functionMap = buildFunctionMap(providers);
        log.debug("FunctionRegistry initialized with {} provider(s): {}", functionMap.size(), functionMap.keySet());
        return new FunctionRegistry(Collections.unmodifiableMap(functionMap));
    }

    private static Map<String, FunctionProvider> buildFunctionMap(List<FunctionProvider> providers) {
        return providers.stream()
                .filter(Objects::nonNull)
                .filter(p -> p.getFunctionName() != null && !p.getFunctionName().isBlank())
                .collect(Collectors.toMap(
                        provider -> provider.getFunctionName().toLowerCase(Locale.ROOT),
                        Function.identity(),
                        (existing, replacement) -> {
                            throw new DuplicateFunctionNameException(
                                    existing.getFunctionName().toLowerCase(Locale.ROOT),
                                    existing.getClass().getSimpleName(),
                                    replacement.getClass().getSimpleName()
                            );
                        }
                ));
    }

    public Optional<FunctionProvider> get(String name) {
        if (name == null) {
            return Optional.empty();
        }
        var key = name.toLowerCase(Locale.ROOT);
        return Optional.ofNullable(availableFunctions.get(key));
    }

    public Set<String> getRegisteredFunctionNames() {
        return availableFunctions.keySet();
    }
}
