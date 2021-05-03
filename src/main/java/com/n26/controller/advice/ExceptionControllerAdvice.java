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

/**
 *
 * Exception Controller Advice takes care of all the runtime exceptions thrown at the controller level
 * and converts them to proper response with appropriate status codes
 *
 * @author Rajagopal
 *
 */
@Slf4j
@RestControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * Advice Handler to process TransactionExpiredException thrown at runtime
     *
     * @return ResponseEntity with NO_CONTENT status code to indicate the transaction given to add is past timeframe
     * */
    @ExceptionHandler(TransactionExpiredException.class)
    public ResponseEntity<ServiceError> handleExpiredTransactionException(TransactionExpiredException exception) {
        log.error("Failed transaction processing for expired transaction!", exception);
        return ResponseEntity.status(NO_CONTENT).body(ServiceError.from(exception));
    }

    /**
     * Advice Handler to process UnrecognizedDataFormatException thrown at runtime
     *
     * @return ResponseEntity with UNPROCESSABLE_ENTITY status code to indicate the transaction given to add has field level data failures
     * */
    @ExceptionHandler(UnrecognizedDataFormatException.class)
    public ResponseEntity<ServiceError> handleUnrecognizedDataFormatException(UnrecognizedDataFormatException exception) {
        log.error("Failed transaction processing for Field level requirements not met!", exception);
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(ServiceError.from(exception));
    }

    /**
     * Advice Handler to process UnrecognizedDataFormatException thrown at runtime
     *
     * @return ResponseEntity with BAD_REQUEST status code to indicate the transaction given to add is Future timeframe
     */
    @ExceptionHandler(FutureTransactionException.class)
    public ResponseEntity<ServiceError> handleFutureTransactionException(FutureTransactionException exception) {
        log.error("Failed transaction processing for Future Transaction data!", exception);
        return ResponseEntity.status(BAD_REQUEST).body(ServiceError.from(exception));
    }

    /**
     * Advice Handler to process Generic exceptions that are thrown at runtime
     *
     * @return ResponseEntity with INTERNAL_SERVER_ERROR status code to indicate the there is a runtime failure
     * */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status, WebRequest request) {
        return super.handleExceptionInternal(ex, ServiceError.from(ex), headers, status, request);
    }

    /**
     * Advice Handler to process Generic exceptions that are thrown at runtime
     *
     * @return ResponseEntity with INTERNAL_SERVER_ERROR status code to indicate the there is a runtime failure
     * */
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