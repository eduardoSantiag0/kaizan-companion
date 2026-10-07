package io.github.eduardosantiag0.kaizan_companion.features.sources.ogs;

import io.github.eduardosantiag0.kaizan_companion.features.sources.contracts.ISourceStrategy;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.GameDetail;
import io.github.eduardosantiag0.kaizan_companion.features.sources.ogs.dtos.PaginatedGameList;
import io.github.eduardosantiag0.sgf.model.SgfGameTree;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Component
@Slf4j
public class OgsGameDownloader implements ISourceStrategy {


    @Value("${ogs.baseurl}")
    private String BASE_URL;
    private final RestTemplate rest = new RestTemplate();

    public OgsGameDownloader() {
    }


    @Override
    public String extractId(String url) {
        String s = "/game/";
        int startPos = url.indexOf(s);
        if (startPos == -1) {
            throw new IllegalArgumentException("Invalid OGS game URL: " + url);
        }

        return url.substring(startPos + s.length());
    }

    @Override
    public ByteArrayResource downloadGameById(String id) {
        String endpoint = BASE_URL + "games/" + id + "/sgf/";
        System.out.println(endpoint);

        try {
            byte[] gameJson = rest.getForObject(endpoint, byte[].class);

            log.debug("Obtidos da url {}", new String(gameJson, StandardCharsets.UTF_8));
             return new ByteArrayResource(gameJson);
        } catch (RestClientException e) {

            System.out.println(endpoint);
            throw new RuntimeException(
                    "Error fetching game " + id,
                    e
            );
        }
    }

    @Override
    public ByteArrayResource downloadLastGame(Long playerId) {

        String endpoint =
                BASE_URL +
                        "players/" +
                        playerId +
                        "/games/?ordering=ended&page=1&page_size=1";

        try {
            PaginatedGameList response =
                    rest.getForObject(endpoint, PaginatedGameList.class);

            assert response != null;
            String url = response
                    .results()
                    .get(0)
                    .related();

            String gameId = url.substring(url.lastIndexOf('/') + 1);

            return downloadGameById(gameId);

        } catch (RestClientException e) {
            throw new RuntimeException(
                    "Error fetching last game for player " + playerId,
                    e
            );
        }
    }

}


