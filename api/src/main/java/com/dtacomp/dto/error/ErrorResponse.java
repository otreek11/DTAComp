package com.dtacomp.dto.error;

public record ErrorResponse(
    int status,
    String error,
    String message,
    String timestamp
) {}
