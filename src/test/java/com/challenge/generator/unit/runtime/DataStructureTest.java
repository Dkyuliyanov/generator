package com.challenge.generator.unit.runtime;

import com.challenge.generator.base.BaseTest;
import com.challenge.generator.expression.engine.model.DataType;
import com.challenge.generator.expression.engine.model.EvaluationContext;
import com.challenge.generator.expression.engine.model.result.EvalResult;
import com.challenge.generator.expression.engine.model.result.MapValue;
import com.challenge.generator.expression.engine.model.result.StringValue;
import com.challenge.generator.expression.engine.parser.ast.ExpressionNode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Data Structure Tests")
class DataStructureTest extends BaseTest {

    @Test
    @DisplayName("Map literal should parse correctly")
    void shouldParseMapLiteral() {
        String expression = "name:John, age:25, city:Boston";
        
        ExpressionNode ast = parser().parse(expression);
        EvaluationContext context = EvaluationContext.from(Map.of());
        EvalResult result = expressionInterpreter.evaluate(ast, context);
        
        assertInstanceOf(MapValue.class, result);
        MapValue mapResult = (MapValue) result;
        
        assertEquals(3, mapResult.values().size());
        assertEquals("John", mapResult.values().get("name"));
        assertEquals("25", mapResult.values().get("age"));
        assertEquals("Boston", mapResult.values().get("city"));
    }

    @Test
    @DisplayName("Map literal with string values should parse correctly")
    void shouldParseMapLiteralWithStringValues() {
        String expression = "greeting:'Hello World', message:'Welcome'";
        
        ExpressionNode ast = parser().parse(expression);
        EvaluationContext context = EvaluationContext.from(Map.of());
        EvalResult result = expressionInterpreter.evaluate(ast, context);
        
        assertInstanceOf(MapValue.class, result);
        MapValue mapResult = (MapValue) result;
        
        assertEquals(2, mapResult.values().size());
        assertEquals("Hello World", mapResult.values().get("greeting"));
        assertEquals("Welcome", mapResult.values().get("message"));
    }

    @Test
    @DisplayName("Single key-value pair should parse correctly")
    void shouldParseSingleKeyValuePair() {
        String expression = "status:active";
        
        ExpressionNode ast = parser().parse(expression);
        EvaluationContext context = EvaluationContext.from(Map.of());
        EvalResult result = expressionInterpreter.evaluate(ast, context);
        
        assertInstanceOf(MapValue.class, result);
        MapValue mapResult = (MapValue) result;
        
        assertEquals(1, mapResult.values().size());
        assertEquals("active", mapResult.values().get("status"));
    }

    @Test
    @DisplayName("Map should return correct data type")
    void shouldReturnCorrectDataType() {
        Map<String, String> entries = Map.of("key", "value");
        MapValue mapResult = new MapValue(entries);
        
        assertEquals(DataType.MAP, mapResult.dataType());
    }

    @Test
    @DisplayName("Map should return first value with firstOrNull")
    void shouldReturnFirstValueWithFirstOrNull() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("first", "value1");
        entries.put("second", "value2");
        MapValue mapResult = new MapValue(entries);
        
        assertEquals("value1", mapResult.firstOrNull());
    }

    @Test
    @DisplayName("Empty map should return null with firstOrNull")
    void shouldReturnNullForEmptyMapWithFirstOrNull() {
        Map<String, String> entries = Map.of();
        MapValue mapResult = new MapValue(entries);
        
        assertNull(mapResult.firstOrNull());
    }

    @Test
    @DisplayName("Map should return values as list with asList")
    void shouldReturnValuesAsListWithAsList() {
        Map<String, String> entries = new LinkedHashMap<>();
        entries.put("key1", "value1");
        entries.put("key2", "value2");
        entries.put("key3", "value3");
        MapValue mapResult = new MapValue(entries);
        
        List<String> expectedValues = List.of("value1", "value2", "value3");
        assertEquals(expectedValues, mapResult.asList());
    }

    @Test
    @DisplayName("Map should work with equals function")
    void shouldWorkWithEqualsFunction() {
        String expression1 = "name:John, age:25";
        String expression2 = "name:John, age:25";
        
        EvaluationContext context = EvaluationContext.from(Map.of());
        
        ExpressionNode ast1 = parser().parse(expression1);
        EvalResult result1 = expressionInterpreter.evaluate(ast1, context);
        
        ExpressionNode ast2 = parser().parse(expression2);
        EvalResult result2 = expressionInterpreter.evaluate(ast2, context);
        

        assertEquals(result1.firstOrNull(), result2.firstOrNull());
    }

    @Test
    @DisplayName("Map should work with zip function")
    void shouldWorkWithZipFunction() {
        String expression = "zip('John', age:25)";
        
        EvaluationContext context = EvaluationContext.from(Map.of());
        ExpressionNode ast = parser().parse(expression);
        EvalResult result = expressionInterpreter.evaluate(ast, context);
        
        assertNotNull(result);

        String concatenated = result.firstOrNull();
        assertNotNull(concatenated);

        assertTrue(concatenated.contains("John"));
        assertTrue(concatenated.contains("25"));
    }

    private record TestCase(String description, String expression, Class<?> expectedType, String expectedFirstValue) {
        Arguments toArg() {
            return Arguments.of(description, expression, expectedType, expectedFirstValue);
        }
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("dataStructureScenarios")
    void shouldHandleVariousDataStructures(String description, String expression, Class<?> expectedType, String expectedFirstValue) {
        EvaluationContext context = EvaluationContext.from(Map.of());
        ExpressionNode ast = parser().parse(expression);
        EvalResult result = expressionInterpreter.evaluate(ast, context);
        
        assertInstanceOf(expectedType, result);
        assertEquals(expectedFirstValue, result.firstOrNull());
    }

    static Stream<Arguments> dataStructureScenarios() {
        return Stream.of(
                new TestCase("should handle simple map", "name:John", MapValue.class, "John").toArg(),
                new TestCase("should handle multi-entry map", "name:John, age:25", MapValue.class, "John").toArg(),
                new TestCase("should handle map with string literals", "msg:'Hello', code:'200'", MapValue.class, "Hello").toArg(),
                new TestCase("should handle single value", "'test'", StringValue.class, "test").toArg()
        );
    }
}
