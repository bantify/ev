package com.qvantel.ev.exception;

import com.qvantel.ev.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.HttpRequestMethodNotSupportedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ---------------------------------------------------------
    // Runtime exceptions
    // ---------------------------------------------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request) {

        log.error(
                "event=APPLICATION_EXCEPTION path={} method={} exception={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getClass().getSimpleName(),
                exception
        );

        ApiError error = new ApiError(
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(error);
    }

    // ---------------------------------------------------------
    // Validation errors
    // ---------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");

        log.warn(
                "event=VALIDATION_FAILED path={} method={} message={}",
                request.getRequestURI(),
                request.getMethod(),
                message
        );

        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    // ---------------------------------------------------------
    // HTTP method not supported
    // ---------------------------------------------------------

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {

        log.warn(
                "event=METHOD_NOT_SUPPORTED path={} method={}",
                request.getRequestURI(),
                request.getMethod()
        );

        ApiError error = new ApiError(
                HttpStatus.METHOD_NOT_ALLOWED.value(),
                "Method Not Allowed",
                "HTTP method " + request.getMethod()
                        + " is not supported for this endpoint",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(error);
    }

    // ---------------------------------------------------------
    // Generic exception
    // ---------------------------------------------------------

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleException(
            Exception exception,
            HttpServletRequest request) {

        log.error(
                "event=INTERNAL_SERVER_ERROR path={} method={} exception={}",
                request.getRequestURI(),
                request.getMethod(),
                exception.getClass().getSimpleName(),
                exception
        );

        ApiError error = new ApiError(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }
}