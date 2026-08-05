package org.epam.learn.gateway.exception;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;

import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GatewayExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayExceptionHandler.class);

    @ExceptionHandler(HttpStatusCodeException.class)
    public ResponseEntity<ErrorResponse> handleRoutingStatusException(HttpStatusCodeException ex,
                                                                      HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        LOGGER.warn("Routing of '{} {}' failed with status {}: {}", request.getMethod(),
                request.getRequestURI(), status.value(), ex.getMessage());
        return errorResponse(status, ErrorMessages.forStatus(status, request.getRequestURI()));
    }

    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<ErrorResponse> handleServiceNotReachable(ResourceAccessException ex,
                                                                   HttpServletRequest request) {
        boolean timedOut = ex.getCause() instanceof SocketTimeoutException;
        HttpStatus status = timedOut ? HttpStatus.GATEWAY_TIMEOUT : HttpStatus.SERVICE_UNAVAILABLE;
        LOGGER.warn("Downstream service is not reachable for '{} {}': {}", request.getMethod(),
                request.getRequestURI(), ex.getMessage());
        return errorResponse(status, ErrorMessages.forStatus(status, request.getRequestURI()));
    }

    @ExceptionHandler({IOException.class, UncheckedIOException.class})
    public ResponseEntity<ErrorResponse> handleProxyIoException(Exception ex, HttpServletRequest request) {
        LOGGER.warn("Proxying of '{} {}' failed: {}", request.getMethod(), request.getRequestURI(),
                ex.getMessage());
        return errorResponse(HttpStatus.BAD_GATEWAY, ErrorMessages.BAD_GATEWAY);
    }

    @ExceptionHandler({NoHandlerFoundException.class, NoResourceFoundException.class})
    public ResponseEntity<ErrorResponse> handleUndefinedRoute(Exception ex, HttpServletRequest request) {
        LOGGER.warn("No route defined for '{} {}'", request.getMethod(), request.getRequestURI());
        return errorResponse(HttpStatus.NOT_FOUND,
                ErrorMessages.NOT_FOUND.formatted(request.getRequestURI()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  HttpServletRequest request) {
        LOGGER.warn("Unsupported method for '{} {}'", request.getMethod(), request.getRequestURI());
        return errorResponse(HttpStatus.METHOD_NOT_ALLOWED,
                ErrorMessages.METHOD_NOT_ALLOWED.formatted(ex.getMethod()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        LOGGER.error("Unexpected error while handling '{} {}'", request.getMethod(),
                request.getRequestURI(), ex);
        return errorResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorMessages.INTERNAL_ERROR);
    }

    private ResponseEntity<ErrorResponse> errorResponse(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(new ErrorResponse(message, String.valueOf(status.value())));
    }
}
