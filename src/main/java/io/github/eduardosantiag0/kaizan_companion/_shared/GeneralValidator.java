package io.github.eduardosantiag0.kaizan_companion._shared;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class GeneralValidator {

    public static boolean isValidURL(String urlString) {
        try {
            URL url = new URL(urlString);
            url.toURI();
            return true;
        } catch (MalformedURLException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
