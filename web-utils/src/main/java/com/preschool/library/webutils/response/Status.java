package com.preschool.library.webutils.response;

public record Status(String code, String message) {
    public Status(String code) {
        this(code, null);
    }
}
