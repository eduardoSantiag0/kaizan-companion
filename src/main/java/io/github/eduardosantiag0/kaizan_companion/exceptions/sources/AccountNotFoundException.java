package io.github.eduardosantiag0.kaizan_companion.exceptions.sources;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
