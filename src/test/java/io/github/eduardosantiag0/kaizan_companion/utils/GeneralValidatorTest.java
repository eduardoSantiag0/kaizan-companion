package io.github.eduardosantiag0.kaizan_companion.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


class GeneralValidatorTest {

    @Test
    void isValidURL_returnsTrue_forWellFormedUrl() {
        assertTrue(GeneralValidator.isValidURL("https://online-go.com/game/123"));
    }

    @Test
    void isValidURL_mustReturnFalse_onMalformedUrl_insteadOfThrowing() {
        assertFalse(GeneralValidator.isValidURL("not a url"));
    }

    @Test
    void isValidURL_mustReturnFalse_onEmptyString_insteadOfThrowing() {
        assertFalse(GeneralValidator.isValidURL(""));
    }

    @Test
    void isValidEmail_rejectsNull() {
        assertFalse(GeneralValidator.isValidEmail(null));
    }

    @Test
    void isValidEmail_rejectsAddressWithoutDomainDot() {
        assertFalse(GeneralValidator.isValidEmail("user@localhost"));
    }

    @Test
    void isValidEmail_acceptsOrdinaryAddress() {
        assertTrue(GeneralValidator.isValidEmail("player@example.com"));
    }
}
