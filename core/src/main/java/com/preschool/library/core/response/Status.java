package com.preschool.library.core.response;

public record Status(String code, String message) {
    public Status(String code) {
        this(code, null);
    }
}
