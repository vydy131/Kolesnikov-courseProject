package com.investagg.exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends AppException {
    public BusinessRuleException(String message) {
        super(message, HttpStatus.UNPROCESSABLE_ENTITY, "BUSINESS_RULE_ERROR");
    }
}
