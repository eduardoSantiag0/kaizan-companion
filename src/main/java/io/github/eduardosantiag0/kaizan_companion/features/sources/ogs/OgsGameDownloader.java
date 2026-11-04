package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs;

import io.github.eduardosantiag0.kaizan_companion.domain.exception.sgf.EmptySgfException;
import io.github.eduardosantiag0.kaizan_companion.domain.exception.sources.LastGameNotFoundException;
import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.PaginatedGameList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.*;

import java.util.Arrays;
import java.util.regex.Pattern;

@Component
@Slf4j
public class OgsGameDownloader implements ISourceStrategy {


    @Value("${ogs.baseurl}")
    private String BASE_URL;
    private final RestTemplate rest = new RestTemplate();
    private static final String QUERY_DELIMITERS = "[/?&]";

    public OgsGameDownloader() {
    }


    @Override
    public String extractId(String url) {
        String s = "/game/";
        int startPos = url.indexOf(s);
        if (startPos == -1) {
            throw new IllegalArgumentException("Invalid OGS game URL: " + url);
        }

        String gameId = url.substring(startPos + s.length());
        String [] idStripped = gameId.splitWithDelimiters(QUERY_DELIMITERS, 0);

        return idStripped[0];
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
        String endpoint = BASE_URL + "games/" + id + "/sgf/";
        System.out.println(endpoint);

        byte[] gameJson = rest.getForObject(endpoint, byte[].class);

        if (gameJson == null || gameJson.length == 0) {
            throw new EmptySgfException(
                    "SGF is empty: " + id
            );
        }
         return new ByteArrayResource(gameJson);
    }

    @Retryable(
            retryFor = {
                    ResourceAccessException.class,
                    HttpServerErrorException.class,
                    HttpClientErrorException.TooManyRequests.class
            },
            maxAttempts = 3,
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    public ByteArrayResource downloadLastGame(Long playerId) {

        String endpoint =
                BASE_URL +
                        "players/" +
                        playerId +
                        "/games/?ordering=-ended" +
                        "&ended__isnull=false" +
                        "&page=1" +
                        "&page_size=1";

        PaginatedGameList response =
                rest.getForObject(endpoint, PaginatedGameList.class);

        String url = null;
        if (response != null) {
            url = response
                    .results()
                    .get(0)
                    .related().detail();

        } else {
            log.error("Resposta nula para: {}", endpoint);
            throw new LastGameNotFoundException("Query: " + endpoint);
        }

        String gameId = url.substring(url.lastIndexOf('/') + 1);

        return downloadGameById(gameId);

    }

}


