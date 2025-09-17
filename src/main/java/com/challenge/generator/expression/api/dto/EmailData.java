package com.challenge.generator.expression.api.dto;

public record EmailData(String id, String value) {
    public static EmailData from(String email) {
        return new EmailData(email, email);
    }
}
