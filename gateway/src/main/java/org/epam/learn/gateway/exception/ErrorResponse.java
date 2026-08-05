package org.epam.learn.gateway.exception;

public record ErrorResponse(String errorMessage, String errorCode) {
}
