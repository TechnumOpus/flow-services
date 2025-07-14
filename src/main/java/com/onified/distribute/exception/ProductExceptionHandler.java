package com.onified.distribute.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice(basePackages = "com.onified.distribute.controller")
@Order(1) // Higher priority than global exception handler
public class ProductExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProductNotFoundException(ProductNotFoundException ex) {
        log.error("Product not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Product Not Found")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ProductAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleProductAlreadyExistsException(ProductAlreadyExistsException ex) {
        log.error("Product already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Product Already Exists")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidProductDataException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProductDataException(InvalidProductDataException ex) {
        log.error("Invalid product data: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Product Data")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ProductOperationException.class)
    public ResponseEntity<ErrorResponse> handleProductOperationException(ProductOperationException ex) {
        log.error("Product operation failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Product Operation Failed")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(ProductValidationException.class)
    public ResponseEntity<ErrorResponse> handleProductValidationException(ProductValidationException ex) {
        log.error("Product validation failed: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Product Validation Failed")
                .message(ex.getMessage())
                .validationErrors(ex.getValidationErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Custom exception classes
    public static class ProductNotFoundException extends RuntimeException {
        public ProductNotFoundException(String message) {
            super(message);
        }

        public ProductNotFoundException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProductAlreadyExistsException extends RuntimeException {
        public ProductAlreadyExistsException(String message) {
            super(message);
        }

        public ProductAlreadyExistsException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidProductDataException extends RuntimeException {
        public InvalidProductDataException(String message) {
            super(message);
        }

        public InvalidProductDataException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProductOperationException extends RuntimeException {
        public ProductOperationException(String message) {
            super(message);
        }

        public ProductOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ProductValidationException extends RuntimeException {
        private final java.util.Map<String, String> validationErrors;

        public ProductValidationException(String message, java.util.Map<String, String> validationErrors) {
            super(message);
            this.validationErrors = validationErrors;
        }

        public java.util.Map<String, String> getValidationErrors() {
            return validationErrors;
        }
    }
}
