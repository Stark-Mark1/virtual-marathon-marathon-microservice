package com.virtualmarathon.marathon.customerror;

import com.virtualmarathon.marathon.Constants;
import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;

@ControllerAdvice
public class ErrorHandler {
    @ExceptionHandler
    public ResponseEntity<ErrorObject> handleException(MarathonClassException e){
        ErrorObject error=new ErrorObject(e.getStatus().value(), e.getMessage(), Constants.getCurrentTime());
        return new ResponseEntity<>(error,e.getStatus());
    }

    @ExceptionHandler
    public ResponseEntity<ErrorObject> handleException(FeignException e){
        ErrorObject error=new ErrorObject(HttpStatus.FAILED_DEPENDENCY.value(), e.contentUTF8(), Constants.getCurrentTime());
        return new ResponseEntity<>(error,HttpStatus.FAILED_DEPENDENCY);
    }
}
