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

    @Test
    void validateHost_mustRejectALookAlikeDomain_thatMerelyEndsWithTheTrustedSuffix() {
        // "fakeonline-go.com" ends with the literal characters "online-go.com", so a naive
        // host.endsWith(domain) check wrongly treats it as the trusted online-go.com host.
        assertFalse(GeneralValidator.validateHost("https://fakeonline-go.com/game/123", "online-go.com"),
                "a host that only ends with the trusted domain as a character suffix must not be accepted");
    }

    @Test
    void validateHost_mustAcceptTheExactTrustedDomain() {
        assertTrue(GeneralValidator.validateHost("https://online-go.com/game/123", "online-go.com"));
    }

    @Test
    void validateHost_mustAcceptARealSubdomainOfTheTrustedDomain() {
        assertTrue(GeneralValidator.validateHost("https://api.online-go.com/game/123", "online-go.com"));
    }

    @Test
    void validateHost_mustRejectAnUnrelatedHost() {
        assertFalse(GeneralValidator.validateHost("https://evil.com/game/123", "online-go.com"));
    }

    @Test
    void validateHost_mustRejectAnUnrelatedHost_evenWhenTheTrustedDomainAppearsInTheQueryString() {
        // The trusted domain must match the URI's *host*, not merely appear anywhere in the URL.
        assertFalse(GeneralValidator.validateHost(
                "http://169.254.169.254/latest/meta-data/?x=https://online-go.com/", "online-go.com"));
    }
}
