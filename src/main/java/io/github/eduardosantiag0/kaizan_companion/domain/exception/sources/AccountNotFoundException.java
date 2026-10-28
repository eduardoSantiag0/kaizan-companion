package io.github.eduardosantiag0.kaizan_companion.domain.exception.sources;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
