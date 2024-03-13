package com.preschool.library.webutils.exception;

import org.springframework.http.HttpStatus;

public class MissingRequestIdHeaderException extends ApplicationException {
    public MissingRequestIdHeaderException() {
        super(
                HttpStatus.BAD_REQUEST.value(),
                "MISSING_REQUEST_ID_HEADER",
                "Missing the X-Request-Id header on your reuqest");
    }
}
