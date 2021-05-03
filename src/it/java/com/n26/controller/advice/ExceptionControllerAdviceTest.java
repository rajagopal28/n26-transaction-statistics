package com.n26.controller.advice;

import com.n26.exception.FutureTransactionException;
import com.n26.exception.TransactionExpiredException;
import com.n26.exception.UnrecognizedDataFormatException;
import junit.framework.TestCase;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionControllerAdviceTest extends TestCase {

    @InjectMocks
    ExceptionControllerAdvice exceptionControllerAdvice;

    @Test
    public void testHandleExpiredTransactionException() {
        ResponseEntity<ExceptionControllerAdvice.ServiceError> serviceErrorResponseEntity = exceptionControllerAdvice.handleExpiredTransactionException(new TransactionExpiredException());
        Assert.assertNotNull(serviceErrorResponseEntity);
        Assert.assertNotNull(serviceErrorResponseEntity.getBody());
        Assert.assertEquals(HttpStatus.NO_CONTENT, serviceErrorResponseEntity.getStatusCode());
    }

    @Test
    public void testHandleUnrecognizedDataFormatException() {
        ResponseEntity<ExceptionControllerAdvice.ServiceError> serviceErrorResponseEntity = exceptionControllerAdvice.handleUnrecognizedDataFormatException(new UnrecognizedDataFormatException());
        Assert.assertNotNull(serviceErrorResponseEntity);
        Assert.assertNotNull(serviceErrorResponseEntity.getBody());
        Assert.assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, serviceErrorResponseEntity.getStatusCode());
    }

    @Test
    public void testHandleFutureTransactionException() {
        ResponseEntity<ExceptionControllerAdvice.ServiceError> serviceErrorResponseEntity = exceptionControllerAdvice.handleFutureTransactionException(new FutureTransactionException());
        Assert.assertNotNull(serviceErrorResponseEntity);
        Assert.assertNotNull(serviceErrorResponseEntity.getBody());
        Assert.assertEquals(HttpStatus.BAD_REQUEST, serviceErrorResponseEntity.getStatusCode());
    }

    @Test
    public void testHandleGenericUnhandledException() {
        ResponseEntity<ExceptionControllerAdvice.ServiceError> serviceErrorResponseEntity = exceptionControllerAdvice.handleUnexpected(new RuntimeException());
        Assert.assertNotNull(serviceErrorResponseEntity);
        Assert.assertNotNull(serviceErrorResponseEntity.getBody());
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, serviceErrorResponseEntity.getStatusCode());
    }

}