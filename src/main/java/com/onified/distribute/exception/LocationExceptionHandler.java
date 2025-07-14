package com.onified.distribute.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
@Order(1) // Higher priority than GlobalExceptionHandler
public class LocationExceptionHandler {

    @ExceptionHandler(LocationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleLocationNotFoundException(LocationNotFoundException ex) {
        log.error("Location not found: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.NOT_FOUND.value())
                .error("Location Not Found")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(LocationAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleLocationAlreadyExistsException(LocationAlreadyExistsException ex) {
        log.error("Location already exists: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.CONFLICT.value())
                .error("Location Already Exists")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(LocationValidationException.class)
    public ResponseEntity<ErrorResponse> handleLocationValidationException(LocationValidationException ex) {
        log.error("Location validation error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Location Validation Error")
                .message(ex.getMessage())
                .validationErrors(ex.getValidationErrors())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LocationOperationException.class)
    public ResponseEntity<ErrorResponse> handleLocationOperationException(LocationOperationException ex) {
        log.error("Location operation error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                .error("Location Operation Failed")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(LocationAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleLocationAccessDeniedException(LocationAccessDeniedException ex) {
        log.error("Location access denied: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.FORBIDDEN.value())
                .error("Location Access Denied")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(LocationServiceException.class)
    public ResponseEntity<ErrorResponse> handleLocationServiceException(LocationServiceException ex) {
        log.error("Location service error: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Location Service Error")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(InvalidLocationStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidLocationStatusException(InvalidLocationStatusException ex) {
        log.error("Invalid location status: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Invalid Location Status")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(LocationHierarchyException.class)
    public ResponseEntity<ErrorResponse> handleLocationHierarchyException(LocationHierarchyException ex) {
        log.error("Location hierarchy error: {}", ex.getMessage());

        ErrorResponse errorResponse = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Location Hierarchy Error")
                .message(ex.getMessage())
                .build();

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    // Custom exception classes
    public static class LocationNotFoundException extends RuntimeException {
        public LocationNotFoundException(String message) {
            super(message);
        }

        public LocationNotFoundException(String locationId, String field) {
            super(String.format("Location not found with %s: %s", field, locationId));
        }
    }

    public static class LocationAlreadyExistsException extends RuntimeException {

        public LocationAlreadyExistsException(String locationId) {
            super(String.format("Location already exists with ID: %s", locationId));
        }
    }

    public static class LocationValidationException extends RuntimeException {
        private final java.util.Map<String, String> validationErrors;

        public LocationValidationException(String message) {
            super(message);
            this.validationErrors = new java.util.HashMap<>();
        }

        public LocationValidationException(String message, java.util.Map<String, String> validationErrors) {
            super(message);
            this.validationErrors = validationErrors;
        }

        public java.util.Map<String, String> getValidationErrors() {
            return validationErrors;
        }
    }

    public static class LocationOperationException extends RuntimeException {
        public LocationOperationException(String message) {
            super(message);
        }

        public LocationOperationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class LocationAccessDeniedException extends RuntimeException {
        public LocationAccessDeniedException(String message) {
            super(message);
        }

        public LocationAccessDeniedException(String locationId, String operation) {
            super(String.format("Access denied for %s operation on location: %s", operation, locationId));
        }
    }

    public static class LocationServiceException extends RuntimeException {
        public LocationServiceException(String message) {
            super(message);
        }

        public LocationServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class InvalidLocationStatusException extends RuntimeException {
        public InvalidLocationStatusException(String message) {
            super(message);
        }

        public InvalidLocationStatusException(String locationId, String currentStatus, String requestedOperation) {
            super(String.format("Cannot perform %s operation on location %s with current status: %s",
                    requestedOperation, locationId, currentStatus));
        }
    }

    public static class LocationHierarchyException extends RuntimeException {
        public LocationHierarchyException(String message) {
            super(message);
        }

        public LocationHierarchyException(String parentId, String childId) {
            super(String.format("Invalid location hierarchy: cannot set %s as parent of %s", parentId, childId));
        }
    }
}
