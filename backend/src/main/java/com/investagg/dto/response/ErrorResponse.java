package com.investagg.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        String error,
        String code,
        OffsetDateTime timestamp,
        List<FieldError> fields
) {
    public ErrorResponse(String error, String code) {
        this(error, code, OffsetDateTime.now(), null);
    }

    public record FieldError(String field, String message) {}
}
