package com.virtualmarathon.marathon.customerror;

import org.springframework.http.HttpStatus;

public class MarathonClassException extends RuntimeException{
    private final HttpStatus status;

    public MarathonClassException(String message, HttpStatus status) {
        super(message);
        this.status=status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
