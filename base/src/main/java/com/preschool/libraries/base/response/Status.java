package com.preschool.libraries.base.response;

public record Status(String code, String message) {
    public Status(String code) {
        this(code, null);
    }
}
