package com.techvvs.inventory.exception;

import org.springframework.http.HttpStatus;

public class CustomException extends RuntimeException {
  public CustomException(String message, HttpStatus httpStatus) {
    this.message = message;
    this.httpStatus = httpStatus;
  }

  @Override
  public String getMessage() {
    return message;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }

  private static final long serialVersionUID = 1L;
  private final String message;
  private final HttpStatus httpStatus;
}
