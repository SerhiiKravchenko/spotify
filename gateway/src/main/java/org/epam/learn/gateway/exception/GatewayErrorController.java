package org.epam.learn.gateway.exception;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GatewayErrorController implements ErrorController {

    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayErrorController.class);

    @RequestMapping(value = "${server.error.path:${error.path:/error}}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
        HttpStatus status = resolveStatus(request);
        String path = resolvePath(request);
        LOGGER.warn("Error dispatch for '{}' with status {}", path, status.value());
        return ResponseEntity.status(status)
                .body(new ErrorResponse(ErrorMessages.forStatus(status, path), String.valueOf(status.value())));
    }

    private HttpStatus resolveStatus(HttpServletRequest request) {
        Object statusCode = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (statusCode instanceof Integer code) {
            HttpStatus status = HttpStatus.resolve(code);
            if (status != null) {
                return status;
            }
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }

    private String resolvePath(HttpServletRequest request) {
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
        return path instanceof String uri ? uri : request.getRequestURI();
    }
}
