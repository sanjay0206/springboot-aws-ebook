package com.aws.spring.ebook.exception;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ErrorDetailsResponse(@JsonSerialize(using = LocalDateTimeSerializer.class) LocalDateTime timestamp,
                                   String message, String details) {
    public ErrorDetailsResponse(LocalDateTime timestamp, String message, String details) {
        this.timestamp = LocalDateTime.parse(timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        this.message = message;
        this.details = details;
    }
}
