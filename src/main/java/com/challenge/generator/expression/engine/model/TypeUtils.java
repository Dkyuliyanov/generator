package com.challenge.generator.expression.engine.model;

/**
 * Minimal, central type inference helpers used by the expression engine.
 * This does not change runtime values, but offers a consistent way to
 * classify strings into DataType values.
 */
public final class TypeUtils {

    private TypeUtils() {
    }

    public static DataType inferType(String s) {
        if (s == null) return DataType.UNKNOWN;
        if (isBoolean(s)) return DataType.BOOLEAN;
        if (isInteger(s)) return DataType.INTEGER;
        if (isDecimal(s)) return DataType.DECIMAL;
        if (isMap(s)) return DataType.MAP;
        return DataType.STRING;
    }

    public static boolean isInteger(String s) {
        if (s == null) return false;
        return s.matches("-?\\d+");
    }

    public static boolean isDecimal(String s) {
        if (s == null) return false;
        return s.matches("-?\\d+\\.\\d+");
    }

    public static boolean isBoolean(String s) {
        if (s == null) return false;
        return "true".equalsIgnoreCase(s) || "false".equalsIgnoreCase(s);
    }

    public static boolean isMap(String s) {
        if (s == null) return false;
        return s.matches("^[^:,]+:[^:,]+(,\\s*[^:,]+:[^:,]+)*$");
    }
}
