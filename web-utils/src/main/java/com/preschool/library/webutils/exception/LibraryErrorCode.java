package com.preschool.library.webutils.exception;

import lombok.Getter;

@Getter
public enum LibraryErrorCode implements ErrorCodeOperation {
    ARGUMENT_NOT_VALID("ARGUMENT_NOT_VALID", "Some argument fields not valid"),
    MISSING_REQUEST_ID_HEADER(
            "MISSING_REQUEST_ID_HEADER", "The X-Request-Id header was missing on your request");

    LibraryErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private String code;
    private String message;
}
