package com.challenge.generator.expression.api.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ExpressionPreprocessor {

    public String preprocess(String expression) {
        if (!StringUtils.hasText(expression)) {
            return expression;
        }

        var result = new StringBuilder(expression.length());
        var quoteState = new QuoteState();

        for (int i = 0; i < expression.length(); i++) {
            char currentChar = expression.charAt(i);

            if (quoteState.isQuoteCharacter(currentChar)) {
                quoteState.toggle(currentChar);
                result.append(currentChar);
                continue;
            }

            if (isPotentialPlusOperator(currentChar, quoteState)) {
                if (isImpliedPlus(expression, i)) {
                    result.append(" + ");
                    i = findEndOfWhitespace(expression, i);
                } else {
                    result.append(currentChar);
                }
            } else {
                result.append(currentChar);
            }
        }

        return result.toString();
    }

    private boolean isPotentialPlusOperator(char c, QuoteState quoteState) {
        return c == ' ' && !quoteState.isInQuotes();
    }

    private boolean isImpliedPlus(String expression, int currentIndex) {
        int prevCharIndex = findPreviousNonWhitespace(expression, currentIndex);
        int nextCharIndex = findNextNonWhitespace(expression, currentIndex);

        if (prevCharIndex == -1 || nextCharIndex == -1) {
            return false;
        }

        char prevChar = expression.charAt(prevCharIndex);
        char nextChar = expression.charAt(nextCharIndex);

        return isOperandEndingChar(prevChar) && isOperandStartingChar(nextChar);
    }

    private boolean isOperandEndingChar(char c) {
        return Character.isLetterOrDigit(c) || c == ')' || c == '\'' || c == '"';
    }

    private boolean isOperandStartingChar(char c) {
        return Character.isLetterOrDigit(c) || c == '(' || c == '\'' || c == '"';
    }

    private int findPreviousNonWhitespace(String text, int fromIndex) {
        for (int i = fromIndex - 1; i >= 0; i--) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findNextNonWhitespace(String text, int fromIndex) {
        for (int i = fromIndex + 1; i < text.length(); i++) {
            if (!Character.isWhitespace(text.charAt(i))) {
                return i;
            }
        }
        return -1;
    }

    private int findEndOfWhitespace(String text, int fromIndex) {
        int i = fromIndex;
        while (i + 1 < text.length() && Character.isWhitespace(text.charAt(i + 1))) {
            i++;
        }
        return i;
    }

    private static class QuoteState {
        private boolean inSingleQuotes = false;
        private boolean inDoubleQuotes = false;

        boolean isQuoteCharacter(char c) {
            return c == '\'' || c == '"';
        }

        void toggle(char quoteChar) {
            if (quoteChar == '\'' && !inDoubleQuotes) {
                inSingleQuotes = !inSingleQuotes;
            } else if (quoteChar == '"' && !inSingleQuotes) {
                inDoubleQuotes = !inDoubleQuotes;
            }
        }

        boolean isInQuotes() {
            return inSingleQuotes || inDoubleQuotes;
        }
    }
}
