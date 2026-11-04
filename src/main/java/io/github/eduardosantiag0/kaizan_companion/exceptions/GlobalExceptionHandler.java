package io.github.eduardosantiag0.kaizan_companion.exceptions;

import io.github.eduardosantiag0.kaizan_companion.exceptions.analysis.AnalysisJobNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.analysis.KataGoAnalysisRejectedException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.analysis.NullAnalysisQueryException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.commands.UnknownCommandException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.messaging.RabbitMqUnavailableException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.sgf.EmptySgfException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.sources.InvalidGameUrlException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.sources.LastGameNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.sources.OgsAccountNotConfiguredException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.sources.AccountNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.storage.StorageException;
import io.github.eduardosantiag0.kaizan_companion.exceptions.telegram.ChatNotificationException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400 - Bad Request
    @ExceptionHandler({
            UnknownCommandException.class,
            InvalidGameUrlException.class,
    })
    public ResponseEntity<ErrorMessageDTO> handleBadRequest(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(request, HttpStatus.BAD_REQUEST, e);
    }

    // 404 - Not Found
    @ExceptionHandler({
            AccountNotFoundException.class,
            InvalidGameUrlException.LatestGameNotFoundException.class,
            AnalysisJobNotFoundException.class,
            LastGameNotFoundException.class
    })
    public ResponseEntity<ErrorMessageDTO> handleNotFound(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(request, HttpStatus.NOT_FOUND, e);
    }

    // 409 - Conflict
    @ExceptionHandler( {
            OgsAccountNotConfiguredException.class,
            InvalidTicketException.class })
    public ResponseEntity<ErrorMessageDTO> handleConflict(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(request, HttpStatus.CONFLICT, e);
    }

    // 502 - Bad Gateway
    @ExceptionHandler({
            EmptySgfException.class,
            KataGoAnalysisRejectedException.class,
            ChatNotificationException.class
    })
    public ResponseEntity<ErrorMessageDTO> handleBadGateway(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(request, HttpStatus.BAD_GATEWAY, e);
    }

    // 503 - Service Unavailable
    @ExceptionHandler(RabbitMqUnavailableException.class)
    public ResponseEntity<ErrorMessageDTO> handleUnavailable(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(request, HttpStatus.SERVICE_UNAVAILABLE, e);
    }

    // 500 - Internal Server Error
    @ExceptionHandler({
            StorageException.class,
            NullAnalysisQueryException.class
    })
    public ResponseEntity<ErrorMessageDTO> handleInternalError(
            RuntimeException e,
            HttpServletRequest request) {

        return createErrorResponse(
                request,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e
        );
    }

    private ResponseEntity<ErrorMessageDTO> createErrorResponse(
            HttpServletRequest request,
            HttpStatus status,
            Exception e) {

        if (status.is5xxServerError()) {
            logger.error("Application error", e);
        } else {
            logger.warn("{}: {}", e.getClass().getSimpleName(), e.getMessage());
        }

        String message = status.is5xxServerError()
                ? "Não foi possível processar a solicitação."
                : e.getMessage();

        ErrorMessageDTO errorMessageDTO = new ErrorMessageDTO()
                .setTimestamp(Instant.now())
                .setStatus(status.value())
                .setException(e.getClass().getSimpleName())
                .setMessage(message)
                .setPath(request.getRequestURI());

        return ResponseEntity.status(status).body(errorMessageDTO);
    }
}
