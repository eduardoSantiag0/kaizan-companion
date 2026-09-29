package io.github.eduardosantiag0.kaizan_companion.domain.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice

public class CustomExceptionHandler {
    private final Logger logger = LoggerFactory.getLogger(CustomExceptionHandler.class);

    private static final HttpStatus INTERNAL_SERVER_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorMessageDTO> handleStorageException(StorageException e, HttpServletRequest request) {
        logger.error("Storage error: ", e);
        return createErrorResponse(request, INTERNAL_SERVER_ERROR_STATUS, e);
    }

    private ResponseEntity<ErrorMessageDTO> createErrorResponse(HttpServletRequest request, HttpStatus status, Exception e) {
        ErrorMessageDTO errorMessageDTO = new ErrorMessageDTO()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setException(e.getClass().getSimpleName())
                .setMessage(e.getMessage())
                .setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(errorMessageDTO);
    }
}
