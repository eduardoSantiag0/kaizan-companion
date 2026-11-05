package io.github.eduardosantiag0.kaizan_companion.utils;

import lombok.experimental.UtilityClass;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

@UtilityClass
public class GeneralValidator {

    public static boolean isValidURL(String urlString) {
        if (urlString == null) {
            return false;
        }

        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            return false;
        }
    }

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    private static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");


    public boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    public boolean isValidUUID (String uuidString) {
        if (uuidString == null) {
            return false;
        }
        return UUID_REGEX.matcher(uuidString).matches();
    }

    public boolean validateHost(String url, String domain) {
        if (url == null) {
            return false;
        }

        try {
            URI uri = new URI(url);
            String host = uri.getHost();

            if (host == null) {
                return false;
            }

            if (host.equals(domain) || host.endsWith("." + domain)) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }
}
