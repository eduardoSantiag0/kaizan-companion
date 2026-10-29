package io.github.eduardosantiag0.kaizan_companion.features.sources.dgs;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.sgf.EmptySgfException;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.dgs.dtos.DgsSgfResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Service
public class DragonServerGameDownloader implements ISourceStrategy {

    @Value("${dgs.baseurl}")
    private String BASE_URL;
    private final RestTemplate rest = new RestTemplate();

    @Override
    public String extractId(String url) {
        String s = "gid=";
        int startPos = url.indexOf(s);
        if (startPos == -1) {
            throw new IllegalArgumentException("Invalid DGS game URL: " + url);
        }

        return url.substring(startPos + s.length());
    }

    @Override
    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    HttpServerErrorException.class,
                    HttpClientErrorException.TooManyRequests.class
            },
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public ByteArrayResource downloadGameById(String id) {
        String endpoint = BASE_URL + "sgf.php?gid=" + id + "&owned_comments=N&quick_mode=1&no_cache=1";

        String rawResponse = rest.getForObject(endpoint, String.class);
        DgsSgfResponseDTO response = DgsSgfResponseDTO.from(rawResponse);

        if (response.error() || response.content() == null || response.content().isBlank()) {
            throw new EmptySgfException(
                    "SGF is empty: " + id
            );
        }

        return new ByteArrayResource(response.content().getBytes(StandardCharsets.UTF_8));
    }

}
