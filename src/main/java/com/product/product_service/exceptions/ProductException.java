package com.product.product_service.exceptions;

import org.springframework.http.HttpStatus;

public class ProductException extends Exception{
  private HttpStatus httpStatus;
  public ProductException(String message) {
    super(message);
  }

  public ProductException(String message, HttpStatus httpStatus) {
    super(message);
    this.httpStatus = httpStatus;
  }

  public HttpStatus getHttpStatus() {
    return httpStatus;
  }
}
