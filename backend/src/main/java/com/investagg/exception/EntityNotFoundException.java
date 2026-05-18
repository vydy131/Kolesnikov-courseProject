package com.investagg.exception;

import org.springframework.http.HttpStatus;

public class EntityNotFoundException extends AppException {
    public EntityNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, "NOT_FOUND");
    }
}
