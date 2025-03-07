package com.product.product_service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlers {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<String> productNotFoundExceptionHandler(ProductNotFoundException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(IllegalAttributeException.class)
    public ResponseEntity<String> illegalAttributeExceptionHandler(IllegalAttributeException e) {
        return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<String> orderExceptionHandler(ProductException productException){
        if (productException.getHttpStatus()!=null)
            return new ResponseEntity<>(productException.getMessage(), productException.getHttpStatus());
        else
            return new ResponseEntity<>(productException.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
