package com.ansari.projects.lovable_clone.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequest(BadRequestException e){
        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, e.getMessage());
        log.error(apiError.toString(), e);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleResourceNotFound(ResourceNotFoundException e){
        ApiError apiError = new ApiError(HttpStatus.NOT_FOUND, e.getResourceName() + " not found with id " + e.getResourceId());
        log.error(apiError.toString(), e);
        return ResponseEntity.status(apiError.status()).body(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValid(MethodArgumentNotValidException e){
        List<ApiFieldError> errors = e.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> new ApiFieldError(error.getField(), error.getDefaultMessage()))
        .toList();

        ApiError apiError = new ApiError(HttpStatus.BAD_REQUEST, "Validation failed", errors);

        log.error(apiError.toString(), e);

        return ResponseEntity.status(apiError.status()).body(apiError);
    }

}
