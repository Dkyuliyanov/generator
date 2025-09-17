package com.challenge.generator.expression.engine.function.provider;

import com.challenge.generator.expression.engine.exception.InvalidDateFormatException;
import com.challenge.generator.expression.engine.function.FunctionProvider;
import com.challenge.generator.expression.engine.function.FunctionName;
import com.challenge.generator.expression.engine.model.result.DateTimeValue;
import com.challenge.generator.expression.engine.model.result.ExecutableExpressionNode;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Locale;

@Component
public class DateProvider implements FunctionProvider {

    @Override
    public String getFunctionName() {
        return FunctionName.DATE.getName();
    }

    @Override
    public ExecutableExpressionNode create(List<ExecutableExpressionNode> arguments) {
        requireArgCount(arguments, 2, getFunctionName());
        ExecutableExpressionNode fmtArg = arguments.get(0);
        ExecutableExpressionNode valArg = arguments.get(1);

        return inputs -> {
            String format = fmtArg.evaluate(inputs).firstOrNull();
            String value = valArg.evaluate(inputs).firstOrNull();

            if (format == null || format.isBlank()) {
                throw new InvalidDateFormatException("date(format, value): format must be a non-empty string");
            }
            if (value == null || value.isBlank()) {
                throw new InvalidDateFormatException("date(format, value): value must be a non-empty string");
            }

            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format, Locale.ROOT);
                TemporalAccessor parsed = formatter.parse(value);
                return new DateTimeValue(toIsoString(parsed));
            } catch (IllegalArgumentException | DateTimeParseException ex) {
                String message = String.format("Invalid date format or value: format='%s' value='%s'. %s", format, value, ex.getMessage());
                throw new InvalidDateFormatException(message);
            }
        };
    }

    private String toIsoString(TemporalAccessor parsed) {
        if (parsed.isSupported(ChronoField.OFFSET_SECONDS)) {
            return OffsetDateTime.from(parsed).toString();
        }
        if (parsed.isSupported(ChronoField.INSTANT_SECONDS)) {
            return ZonedDateTime.from(parsed).toOffsetDateTime().toString();
        }
        if (parsed.isSupported(ChronoField.HOUR_OF_DAY)) {
            return LocalDateTime.from(parsed).toString();
        }
        return LocalDate.from(parsed).toString();
    }
}
