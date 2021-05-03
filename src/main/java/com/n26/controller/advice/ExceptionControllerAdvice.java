package com.n26.controller.advice;

import com.n26.exception.FutureTransactionException;
import com.n26.exception.TransactionExpiredException;
import com.n26.exception.UnrecognizedDataFormatException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.validation.constraints.NotNull;

import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TransactionExpiredException.class)
    public ResponseEntity<ServiceError> handleExpiredTransactionException(TransactionExpiredException exception) {
        log.error("Failed transaction processing for expired transaction!", exception);
        return ResponseEntity.status(NO_CONTENT).body(ServiceError.from(exception));
    }

    @ExceptionHandler(UnrecognizedDataFormatException.class)
    public ResponseEntity<ServiceError> handleUnrecognizedDataFormatException(UnrecognizedDataFormatException exception) {
        log.error("Failed transaction processing for Past Transaction data!", exception);
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(ServiceError.from(exception));
    }

    @ExceptionHandler(FutureTransactionException.class)
    public ResponseEntity<ServiceError> handleFutureTransactionException(FutureTransactionException exception) {
        log.error("Failed transaction processing for Future Transaction data!", exception);
        return ResponseEntity.status(BAD_REQUEST).body(ServiceError.from(exception));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, ServiceError.from(ex), headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ServiceError> handleUnexpected(Exception exception) {
        log.error("Unexpected exception", exception);
        return ResponseEntity.status(INTERNAL_SERVER_ERROR).body(new ServiceError(exception.getMessage(), null));
    }

    @Data
    public static class ServiceError {
        @NotNull
        private final String errorMessage;
        private final Object details;

        public static ServiceError from(RuntimeException paymentException) {
            return new ServiceError(paymentException.getMessage(), paymentException.getCause());
        }

        public static ServiceError from(MethodArgumentNotValidException ex) {
            return new ServiceError("Validation failed", ex.getBindingResult().getAllErrors());
        }
    }
}