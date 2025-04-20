package com.preschool.libraries.base.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class ApplicationException extends RuntimeException {
  private int httpStatus = 555;
  private String code;
  private String message;
}
