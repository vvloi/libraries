package com.preschool.library.webutils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class ApplicationException extends RuntimeException {
    private String code;
    private String message;
}
