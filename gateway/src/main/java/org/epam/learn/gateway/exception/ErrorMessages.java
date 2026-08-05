package org.epam.learn.gateway.exception;

import org.springframework.http.HttpStatus;

final class ErrorMessages {

    static final String SERVICE_UNAVAILABLE =
            "The requested service is temporarily unavailable. Please try again later";
    static final String GATEWAY_TIMEOUT =
            "The requested service did not respond in time. Please try again later";
    static final String BAD_GATEWAY =
            "The requested service returned an invalid response. Please try again later";
    static final String NOT_FOUND = "The requested endpoint '%s' does not exist";
    static final String METHOD_NOT_ALLOWED = "Method '%s' is not supported for this endpoint";
    static final String INTERNAL_ERROR =
            "An unexpected error occurred while processing the request. Please try again later";

    private ErrorMessages() {
    }

    static String forStatus(HttpStatus status, String path) {
        return switch (status) {
            case NOT_FOUND -> NOT_FOUND.formatted(path);
            case SERVICE_UNAVAILABLE -> SERVICE_UNAVAILABLE;
            case GATEWAY_TIMEOUT -> GATEWAY_TIMEOUT;
            case BAD_GATEWAY -> BAD_GATEWAY;
            default -> status.is4xxClientError() ? status.getReasonPhrase() : INTERNAL_ERROR;
        };
    }
}
