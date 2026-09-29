package io.github.eduardosantiag0.kaizan_companion.domain.exception;

import java.time.Instant;

public class ErrorMessageDTO {
    private Instant timestamp;
    private Integer status;
    private String exception;
    private String message;
    private String path;

    public ErrorMessageDTO() {}

    public ErrorMessageDTO(Instant timestamp, Integer status, String exception, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.exception = exception;
        this.message = message;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public ErrorMessageDTO setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Integer getStatus() {
        return status;
    }

    public ErrorMessageDTO setStatus(Integer status) {
        this.status = status;
        return this;
    }

    public String getException() {
        return exception;
    }

    public ErrorMessageDTO setException(String exception) {
        this.exception = exception;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ErrorMessageDTO setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getPath() {
        return path;
    }

    public ErrorMessageDTO setPath(String path) {
        this.path = path;
        return this;
    }
}

