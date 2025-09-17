package com.challenge.generator.expression.api.util;

import java.util.List;
import java.util.regex.Pattern;

public class EmailValidator {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    public static boolean containsInvalidEmails(List<String> emails) {
        return emails.stream().anyMatch(email -> !isValidEmail(email));
    }
    
    public static List<String> getInvalidEmails(List<String> emails) {
        return emails.stream()
                .filter(email -> !isValidEmail(email))
                .toList();
    }
}
